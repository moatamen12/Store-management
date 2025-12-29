package univ.StockManger.StockManger.service;

import com.lowagie.text.DocumentException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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
import java.util.Locale;

@Service
public class ReportService {

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
    public void generateMonthlyReport() throws DocumentException, IOException {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate startDate = lastMonth.atDay(1);
        LocalDate endDate = lastMonth.atEndOfMonth();

        generateReport(startDate, endDate, ReportType.MONTHLY, null);
    }

    public void generateCustomReport(LocalDate startDate, LocalDate endDate, User generatedBy) throws DocumentException, IOException {
        if (startDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the future.");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }
        generateReport(startDate, endDate, ReportType.CUSTOM, generatedBy);
    }

    private void generateReport(LocalDate startDate, LocalDate endDate, ReportType type, User generatedBy) throws DocumentException, IOException {
        List<Bon> bons = bonRepository.findAllByDateBetween(startDate, endDate);
        double totalExpense = bons.stream()
                .flatMap(bon -> bon.getLignesBon().stream())
                .mapToDouble(ligne -> {
                    try {
                        if (ligne.getProduit() != null) {
                            return ligne.getQuantite() * ligne.getProduit().getPrixUnitaire();
                        }
                    } catch (Exception e) {
                        // Product was deleted physically or logically, ignore or log
                        return 0.0;
                    }
                    return 0.0;
                })
                .sum();

        Rapport rapport = Rapport.builder()
                .reportType(type)
                .dateGeneration(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .totalExpense(totalExpense)
                .generatedBy(generatedBy)
                .build();

        byte[] pdfBytes = pdfGeneratorService.generatePdf(rapport, bons);
        String fileName = "report_" + type.name().toLowerCase() + "_" + System.currentTimeMillis() + ".pdf";
        Path path = Paths.get("uploads/" + fileName);

        // Ensure directory exists
        if (!Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        Files.write(path, pdfBytes);

        // Save only the filename to the database
        rapport.setFilePath(fileName);
        rapportRepository.save(rapport);

        // Notify SG if it's an automated monthly report
        if (type == ReportType.MONTHLY) {
            List<User> sgs = userRepository.findAllByRole(Role.Secretaire_General);
            for (User sg : sgs) {
                notificationService.createNotification(this, NotificationType.REPORT_GENERATED,
                        "Monthly report generated for " + startDate.getMonth() + " " + startDate.getYear(),
                        rapport.getId(), sg.getId());
            }
        }
    }
}
