package univ.StockManger.StockManger.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.StockManger.StockManger.Repositories.DemandesRepository;
import univ.StockManger.StockManger.Repositories.ProduitsRepository;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class requesterController {

    @Autowired
    private ProduitsRepository produitsRepository;
    @Autowired
    private DemandesRepository demandesRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/requester")
    public String requesterDashboard(Model model, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            List<Demandes> requests = demandesRepository.findTop10RecentForDemandeur(user.getId());
            model.addAttribute("requests", requests);

            model.addAttribute("pendingCount", requests.stream().filter(r -> r.getEtat_demande() == RequestStatus.PENDING).count());
            model.addAttribute("approvedCount", requests.stream().filter(r -> r.getEtat_demande() == RequestStatus.APPROVED).count());
            model.addAttribute("rejectedCount", requests.stream().filter(r -> r.getEtat_demande() == RequestStatus.REJECTED).count());
            model.addAttribute("deliveredCount", requests.stream().filter(r -> r.getEtat_demande() == RequestStatus.DELIVERED).count());
        }
        return "requester";
    }

    @PostMapping("/requester/request")
    public String submitRequest(
            @RequestParam(value = "selectedProducts", required = false) Long[] productIds,
            @RequestParam Map<String, String> allRequestParams,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (productIds == null || productIds.length == 0) {
            redirectAttributes.addFlashAttribute("error", "No products selected.");
            return "redirect:/stock";
        }

        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/stock";
        }

        Demandes demande = new Demandes();
        demande.setDemandeur(user);
        demande.setEtat_demande(RequestStatus.PENDING);
        demande.setRequest_date(java.time.LocalDate.now());

        for (Long productId : productIds) {
            String qtyStr = allRequestParams.get("quantities[" + productId + "]");
            int requestedQty;
            try {
                requestedQty = Integer.parseInt(qtyStr);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Invalid quantity for product ID " + productId);
                return "redirect:/stock";
            }

            Produits product = produitsRepository.findById(productId).orElse(null);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Product not found.");
                return "redirect:/stock";
            }
            if (requestedQty > product.getQuantite()) {
                redirectAttributes.addFlashAttribute("error", "Requested quantity for " + product.getNom() + " exceeds available stock.");
                return "redirect:/stock";
            }

            product.setQuantite(product.getQuantite() - requestedQty);
            produitsRepository.save(product);

            LigneDemande ligne = new LigneDemande();
            ligne.setProduit(product);
            ligne.setQuantiteDemandee(requestedQty);
            ligne.setDemande(demande);
            demande.getLignes().add(ligne);
        }

        demandesRepository.save(demande);

        redirectAttributes.addFlashAttribute("success", "Request submitted successfully.");
        return "redirect:/stock";
    }

}
