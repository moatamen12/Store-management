package univ.StockManger.StockManger.Controller;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.StockManger.StockManger.Repositories.DemandesRepository;
import univ.StockManger.StockManger.Repositories.ProduitsRepository;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.*;
import univ.StockManger.StockManger.events.NotificationType;
import univ.StockManger.StockManger.service.NotificationService;

import java.time.LocalDate;
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

    public SGController(DemandesRepository demandesRepository, ProduitsRepository produitsRepository, UserRepository userRepository, NotificationService notificationService, MessageSource messageSource) {
        this.demandesRepository = demandesRepository;
        this.produitsRepository = produitsRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.messageSource = messageSource;
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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}
