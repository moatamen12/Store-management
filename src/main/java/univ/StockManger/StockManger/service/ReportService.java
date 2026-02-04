package univ.StockManger.StockManger.service;

import com.lowagie.text.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import univ.StockManger.StockManger.Repositories.BonRepository;
import univ.StockManger.StockManger.Repositories.RapportRepository;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.*;
import univ.StockManger.StockManger.events.NotificationType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private RapportRepository rapportRepository;

    @Autowired
    private BonRepository bonRepository;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageSource messageSource;

    @Scheduled(cron = "0 0 0 1 * ?") // Run on the first day of every month
    @Transactional
    public void generateMonthlyReport() throws DocumentException, IOException {
        logger.info("Starting monthly report generation task.");
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate startDate = lastMonth.atDay(1);
        LocalDate endDate = lastMonth.atEndOfMonth();

        logger.info("Generating monthly report for period: {} to {}", startDate, endDate);

        if (rapportRepository.existsByReportTypeAndStartDateAndEndDate(ReportType.MONTHLY, startDate, endDate)) {
            logger.info("Monthly report for period {} to {} already exists. Skipping generation.", startDate, endDate);
            return;
        }

        logger.info("No existing report found. Proceeding with new report generation.");
        generateReport(startDate, endDate, ReportType.MONTHLY, null);
        logger.info("Finished monthly report generation task.");
    }

    @Transactional
    public void generateCustomReport(LocalDate startDate, LocalDate endDate, User generatedBy) throws DocumentException, IOException {
        logger.info("Starting custom report generation task for user: {}", generatedBy.getEmail());
        if (startDate.isAfter(LocalDate.now())) {
            logger.error("Start date {} is in the future.", startDate);
            throw new IllegalArgumentException("Start date cannot be in the future.");
        }
        if (startDate.isAfter(endDate)) {
            logger.error("Start date {} is after end date {}.", startDate, endDate);
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
        generateReport(startDate, endDate, ReportType.CUSTOM, generatedBy);
        logger.info("Finished custom report generation task for user: {}", generatedBy.getEmail());
    }

    private void generateReport(LocalDate startDate, LocalDate endDate, ReportType type, User generatedBy) throws DocumentException, IOException {
        logger.info("Generating report of type {} from {} to {}. Initiated by: {}", type, startDate, endDate, generatedBy != null ? generatedBy.getEmail() : "SYSTEM");
        List<Bon> bons = bonRepository.findAllByDateBetween(startDate, endDate);
        logger.info("Found {} transactions for the period.", bons.size());

        double totalExpense = bons.stream()
                .flatMap(bon -> bon.getLignesBon().stream())
                .mapToDouble(ligne -> {
                    try {
                        if (ligne.getProduit() != null) {
                            double expense = ligne.getQuantite() * ligne.getProduit().getPrixUnitaire();
                            logger.debug("Calculating expense for product {}: {} * {} = {}", ligne.getProduit().getNom(), ligne.getQuantite(), ligne.getProduit().getPrixUnitaire(), expense);
                            return expense;
                        }
                    } catch (Exception e) {
                        logger.warn("Could not calculate expense for a line item, possibly due to a deleted product.", e);
                        return 0.0;
                    }
                    return 0.0;
                })
                .sum();
        logger.info("Total calculated expense: {}", totalExpense);

        Rapport rapport = Rapport.builder()
                .reportType(type)
                .dateGeneration(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .totalExpense(totalExpense)
                .generatedBy(generatedBy)
                .build();
        logger.info("Rapport object created: {}", rapport);

        byte[] pdfBytes = pdfGeneratorService.generatePdf(rapport, bons);
        String fileName = "report_" + type.name().toLowerCase() + "_" + System.currentTimeMillis() + ".pdf";
        Path path = Paths.get("uploads/" + fileName);

        if (!Files.exists(path.getParent())) {
            logger.info("Creating directory for uploads: {}", path.getParent());
            Files.createDirectories(path.getParent());
        }

        Files.write(path, pdfBytes);
        logger.info("PDF report saved to: {}", path.toAbsolutePath());

        rapport.setFilePath(fileName);
        rapportRepository.save(rapport);
        logger.info("Report metadata saved to the database with ID: {}", rapport.getId());

        if (type == ReportType.MONTHLY) {
            logger.info("Monthly report generated. Notifying all Secretaire_General users.");
            List<User> sgs = userRepository.findAllByRole(Role.Secretaire_General);
            for (User sg : sgs) {
                logger.info("Sending notification to user: {}", sg.getEmail());
                notificationService.createNotification(this, NotificationType.REPORT_GENERATED,
                        "Monthly report generated for " + startDate.getMonth() + " " + startDate.getYear(),
                        rapport.getId(), sg.getId());
            }
            logger.info("Finished notifying users.");
        }
    }
}
