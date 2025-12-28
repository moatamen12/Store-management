package univ.StockManger.StockManger.Controller;

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
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SGController {

    private final DemandesRepository demandesRepository;
    private final ProduitsRepository produitsRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public SGController(DemandesRepository demandesRepository, ProduitsRepository produitsRepository, UserRepository userRepository, NotificationService notificationService) {
        this.demandesRepository = demandesRepository;
        this.produitsRepository = produitsRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/sg")
    public String sgDashboard(Model model) {
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
                    : "deleted user";
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
    public String sgRequests(Model model){
        List<Demandes> requests = demandesRepository.findAll();
        requests.forEach(d -> d.setLignes(d.getLignes().stream().filter(l -> l.getProduit() != null).collect(Collectors.toList())));
        requests.sort(Comparator.comparing(d -> d.getEtat_demande() != RequestStatus.PENDING));
        List<Map<String, Object>> requestViews = requests.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("demande", d);
            String userName = (d.getDemandeur() != null)
                    ? d.getDemandeur().getNom() + " " + d.getDemandeur().getPrenom()
                    : "deleted user";
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
                                 RedirectAttributes redirectAttributes){
        Optional<Demandes> opt = demandesRepository.findById(id);
        if(opt.isPresent()){
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
                notificationService.createNotification(this, NotificationType.REQUEST_APPROVED, "Your request #" + demande.getId() + " has been approved.", demande.getId(), demande.getDemandeur().getId());
            }

            // Notify all storekeepers (magasiniers)
            List<User> magasiniers = userRepository.findAllByRole(Role.magasinier);
            for (User magasinier : magasiniers) {
                notificationService.createNotification(this, NotificationType.REQUEST_APPROVED,
                        "Request #" + demande.getId() + " has been approved and is ready for delivery.",
                        demande.getId(), magasinier.getId());
            }

            redirectAttributes.addFlashAttribute("success", "Request approved successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Request not approved.");
        }
        return "redirect:" + (returnUrl != null && !returnUrl.isEmpty() ? returnUrl : "/sg/requests");
    }

    @PostMapping("/sg/update/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                                @RequestParam(required = false, name = "commentaire") String commentaire,
                                @RequestParam(required = false) String returnUrl,
                                RedirectAttributes redirectAttributes){
        Optional<Demandes> opt = demandesRepository.findById(id);
        if(opt.isPresent()){
            Demandes demande = opt.get();
            demande.setEtat_demande(RequestStatus.REJECTED);
            if(commentaire != null && !commentaire.trim().isEmpty()){
                demande.setCommentaire(commentaire);
            }
            
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                demande.setValidatedBy(currentUser);
            }
            
            demande.setValidation_date(LocalDate.now());
            
            demandesRepository.save(demande);

            if (demande.getDemandeur() != null) {
                notificationService.createNotification(this, NotificationType.REQUEST_REJECTED, "Your request #" + demande.getId() + " has been rejected.", demande.getId(), demande.getDemandeur().getId());
            }

            redirectAttributes.addFlashAttribute("success", "Request rejected successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Request not rejected.");
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
