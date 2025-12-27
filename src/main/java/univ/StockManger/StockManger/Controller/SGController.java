package univ.StockManger.StockManger.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.StockManger.StockManger.Repositories.DemandesRepository;
import univ.StockManger.StockManger.Repositories.ProduitsRepository;
import univ.StockManger.StockManger.entity.Demandes;
import univ.StockManger.StockManger.entity.Produits;
import univ.StockManger.StockManger.entity.RequestStatus;

import java.util.*;

@Controller
public class SGController {

    private final DemandesRepository demandesRepository;
    private final ProduitsRepository produitsRepository;

    public SGController(DemandesRepository demandesRepository, ProduitsRepository produitsRepository) {
        this.demandesRepository = demandesRepository;
        this.produitsRepository = produitsRepository;
    }

    @GetMapping("/sg")
    public String sgDashboard(Model model) {
        List<Demandes> allRequests = demandesRepository.findAll();
        List<Produits> allProducts = produitsRepository.findAll();

        long totalRequests = allRequests.size();
        long pendingRequests = allRequests.stream().filter(d -> d.getEtat_demande() == RequestStatus.PENDING).count();
        
        List<Produits> lowStockProducts = allProducts.stream()
                .filter(p -> p.getQuantite() <= p.getSeuilAlerte())
                .toList();
        
        long lowStockCount = lowStockProducts.size();
        
        // Limit low stock items for dashboard display (e.g., top 5)
        List<Produits> lowStockDisplay = lowStockProducts.stream()
                .limit(5)
                .toList();

        // Filter for PENDING requests only, then sort by ID descending
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

    @GetMapping("/magasinier")
    public String sg(Model model) {
        return "magasinier";
    }


    // single handler for the requests page (removed duplicate mapping)
    @GetMapping("/sg/requests")
    public String sgRequests(Model model){
        List<Demandes> requests = demandesRepository.findAll();
        requests.sort(Comparator.comparing(d -> d.getEtat_demande() != RequestStatus.PENDING));
        // Map each request to a DTO with user name or "deleted user"
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

    @GetMapping("/sg/stock")
    public String sgStock(Model model) {
        List<Produits> products = produitsRepository.findAll();
        model.addAttribute("products", products);
        return "sg_stock";
    }

    @PostMapping("/sg/update/{id}/approve")
    public String approveRequest(@PathVariable Long id, 
                                 @RequestParam(required = false) String returnUrl,
                                 RedirectAttributes redirectAttributes){
        Optional<Demandes> opt = demandesRepository.findById(id);
        if(opt.isPresent()){
            Demandes demande = opt.get();
            demande.setEtat_demande(RequestStatus.APPROVED);
            demandesRepository.save(demande);
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
            demandesRepository.save(demande);
            redirectAttributes.addFlashAttribute("success", "Request rejected successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Request not rejected.");
        }
        return "redirect:" + (returnUrl != null && !returnUrl.isEmpty() ? returnUrl : "/sg/requests");
    }
}
