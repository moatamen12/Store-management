package univ.StockManger.StockManger.Controller;

import com.lowagie.text.DocumentException;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.StockManger.StockManger.Repositories.*;
import univ.StockManger.StockManger.entity.*;
import univ.StockManger.StockManger.events.NotificationType;
import univ.StockManger.StockManger.service.NotificationService;
import univ.StockManger.StockManger.service.PdfGeneratorService;
import univ.StockManger.StockManger.service.ReportService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class SGController {

    private final DemandesRepository demandesRepository;
    private final ProduitsRepository produitsRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final MessageSource messageSource;
    private final RapportRepository rapportRepository;
    private final ReportService reportService;
    private final BonRepository bonRepository;
    private final PdfGeneratorService pdfGeneratorService;

    public SGController(DemandesRepository demandesRepository, ProduitsRepository produitsRepository, UserRepository userRepository, NotificationService notificationService, MessageSource messageSource, RapportRepository rapportRepository, ReportService reportService, BonRepository bonRepository, PdfGeneratorService pdfGeneratorService) {
        this.demandesRepository = demandesRepository;
        this.produitsRepository = produitsRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.messageSource = messageSource;
        this.rapportRepository = rapportRepository;
        this.reportService = reportService;
        this.bonRepository = bonRepository;
        this.pdfGeneratorService = pdfGeneratorService;
    }

    @GetMapping("/sg/report/{id}/download")
    public ResponseEntity<byte[]> downloadReportPdf(@PathVariable Long id) throws DocumentException, IOException {
        Rapport rapport = rapportRepository.findById(id).orElse(null);
        if (rapport == null) {
            return ResponseEntity.notFound().build();
        }

        List<Bon> bons = bonRepository.findAllByDateBetween(rapport.getStartDate(), rapport.getEndDate());
        byte[] pdf = pdfGeneratorService.generatePdf(rapport, bons);

        String fileName = "report-" + rapport.getId() + "-" + rapport.getDateGeneration() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/sg")
    public String sgDashboard(Model model, Locale locale) {
        List<Demandes> allRequests = demandesRepository.findAll();
        allRequests.forEach(d -> d.setLignes(d.getLignes().stream().filter(l -> l.getProduit() != null).collect(Collectors.toList())));
        List<Produits> allProducts = produitsRepository.findAll();

        long totalRequests = allRequests.size();
        long pendingRequests = allRequests.stream().filter(d -> d.getEtat_demande() == RequestStatus.PENDING).count();

        List<Produits> lowStockProducts = allProducts.stream()
                .filter(p -> p.getQuantite() <= p.getSeuilAlerte())
                .toList();

        long lowStockCount = lowStockProducts.size();

        List<Produits> lowStockDisplay = lowStockProducts.stream()
                .limit(5)
                .toList();

        List<Demandes> recentPendingRequests = allRequests.stream()
                .filter(d -> d.getEtat_demande() == RequestStatus.PENDING)
                .sorted(Comparator.comparing((Demandes d) -> d.getId()).reversed())
                .limit(5)
                .toList();

        List<Map<String, Object>> recentRequestViews = recentPendingRequests.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("demande", d);
            String userName = (d.getDemandeur() != null)
                    ? d.getDemandeur().getNom() + " " + d.getDemandeur().getPrenom()
                    : messageSource.getMessage("user.deleted", null, locale);
            map.put("demandeurName", userName);

            double totalPrice = d.getLignes().stream()
                    .mapToDouble(l -> l.getQuantiteDemandee() * (l.getProduit() != null ? l.getProduit().getPrixUnitaire() : 0.0))
                    .sum();
            map.put("totalPrice", totalPrice);

            return map;
        }).toList();

        model.addAttribute("totalRequests", totalRequests);
        model.addAttribute("pendingRequests", pendingRequests);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("recentRequests", recentRequestViews);
        model.addAttribute("lowStockProducts", lowStockDisplay);

        return "sg";
    }

    @GetMapping("/sg/requests")
    public String sgRequests(Model model, Locale locale) {
        List<Demandes> requests = demandesRepository.findAll();
        requests.forEach(d -> d.setLignes(d.getLignes().stream().filter(l -> l.getProduit() != null).collect(Collectors.toList())));
        requests.sort(Comparator.comparing(d -> d.getEtat_demande() != RequestStatus.PENDING));
        List<Map<String, Object>> requestViews = requests.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("demande", d);
            String userName = (d.getDemandeur() != null)
                    ? d.getDemandeur().getNom() + " " + d.getDemandeur().getPrenom()
                    : messageSource.getMessage("user.deleted", null, locale);
            map.put("demandeurName", userName);

            double totalPrice = d.getLignes().stream()
                    .mapToDouble(l -> l.getQuantiteDemandee() * (l.getProduit() != null ? l.getProduit().getPrixUnitaire() : 0.0))
                    .sum();
            map.put("totalPrice", totalPrice);

            return map;
        }).toList();
        model.addAttribute("requests", requestViews);
        return "sg_requists";
    }

    @PostMapping("/sg/update/{id}/approve")
    public String approveRequest(@PathVariable Long id,
                                 @RequestParam(required = false) String returnUrl,
                                 RedirectAttributes redirectAttributes, Locale locale) {
        Optional<Demandes> opt = demandesRepository.findById(id);
        if (opt.isPresent()) {
            Demandes demande = opt.get();
            demande.setEtat_demande(RequestStatus.APPROVED);

            User currentUser = getCurrentUser();
            if (currentUser != null) {
                demande.setValidatedBy(currentUser);
            }

            demande.setValidation_date(LocalDate.now());
            demandesRepository.save(demande);

            // Notify the original requester
            if (demande.getDemandeur() != null) {
                notificationService.createNotification(this, NotificationType.REQUEST_APPROVED, messageSource.getMessage("notification.request.approved", new Object[]{demande.getId()}, locale), demande.getId(), demande.getDemandeur().getId());
            }

            // Notify all storekeepers (magasiniers)
            List<User> magasiniers = userRepository.findAllByRole(Role.magasinier);
            for (User magasinier : magasiniers) {
                notificationService.createNotification(this, NotificationType.REQUEST_APPROVED,
                        messageSource.getMessage("notification.request.approved.for.delivery", new Object[]{demande.getId()}, locale),
                        demande.getId(), magasinier.getId());
            }

            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("request.success.approved", null, locale));
        } else {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.notApproved", null, locale));
        }
        return "redirect:" + (returnUrl != null && !returnUrl.isEmpty() ? returnUrl : "/sg/requests");
    }

    @PostMapping("/sg/update/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                                @RequestParam(required = false, name = "commentaire") String commentaire,
                                @RequestParam(required = false) String returnUrl,
                                RedirectAttributes redirectAttributes, Locale locale) {
        Optional<Demandes> opt = demandesRepository.findById(id);
        if (opt.isPresent()) {
            Demandes demande = opt.get();

            if (demande.getEtat_demande() == RequestStatus.DELIVERED) {
                for (LigneDemande ligne : demande.getLignes()) {
                    Produits produit = ligne.getProduit();
                    int quantiteDemandee = ligne.getQuantiteDemandee();
                    produit.setQuantite(produit.getQuantite() + quantiteDemandee);
                    produitsRepository.save(produit);
                }
            }

            demande.setEtat_demande(RequestStatus.REJECTED);
            if (commentaire != null && !commentaire.trim().isEmpty()) {
                demande.setCommentaire(commentaire);
            }

            User currentUser = getCurrentUser();
            if (currentUser != null) {
                demande.setValidatedBy(currentUser);
            }

            demande.setValidation_date(LocalDate.now());

            demandesRepository.save(demande);

            if (demande.getDemandeur() != null) {
                notificationService.createNotification(this, NotificationType.REQUEST_REJECTED, messageSource.getMessage("notification.request.rejected", new Object[]{demande.getId()}, locale), demande.getId(), demande.getDemandeur().getId());
            }

            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("request.success.rejected", null, locale));
        } else {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.notRejected", null, locale));
        }
        return "redirect:" + (returnUrl != null && !returnUrl.isEmpty() ? returnUrl : "/sg/requests");
    }

    @GetMapping("/sg/reports")
    public String showReports(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(required = false) String keyword) {
        int pageSize = 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("dateGeneration").descending());
        Page<Rapport> reportPage;

        if (keyword != null && !keyword.isEmpty()) {
            reportPage = rapportRepository.search(keyword, pageable);
        } else {
            reportPage = rapportRepository.findAll(pageable);
        }

        model.addAttribute("reports", reportPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        return "sg_reports";
    }

    @PostMapping("/sg/reports/generate")
    public String generateCustomReport(@RequestParam("startDate") LocalDate startDate,
                                       @RequestParam("endDate") LocalDate endDate,
                                       RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            reportService.generateCustomReport(startDate, endDate, currentUser);
            redirectAttributes.addFlashAttribute("success", "Custom report generated successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error generating report.");
        }
        return "redirect:/sg/reports";
    }

    // UPDATED: This now generates a report for the CURRENT month for testing purposes
    @GetMapping("/sg/generate-report")
    public String generateReport(RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            YearMonth currentMonth = YearMonth.now();
            LocalDate startDate = currentMonth.atDay(1);
            LocalDate endDate = currentMonth.atEndOfMonth();
            
            // Use generateCustomReport to create a report for THIS month
            reportService.generateCustomReport(startDate, endDate, currentUser);
            
            redirectAttributes.addFlashAttribute("success", "Report generated successfully for current month!");
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error generating report.");
        }
        return "redirect:/sg/reports";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}
