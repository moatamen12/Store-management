package univ.StockManger.StockManger.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public String requesterDashboard() {
        return "requester";
    }

    @GetMapping("/requester/products")
    public String showProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Produits> products;
        if (search != null && !search.isEmpty()) {
            products = produitsRepository.findByNomContainingIgnoreCase(search, pageable);
        } else {
            products = produitsRepository.findAll(pageable);
        }
        model.addAttribute("products", products.getContent());
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        return "requester_products";
    }

    @PostMapping("/requester/request")
    public String submitRequest(
            @RequestParam(value = "selectedProducts", required = false) Long[] productIds,
            @RequestParam Map<String, String> allRequestParams,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (productIds == null || productIds.length == 0) {
            redirectAttributes.addFlashAttribute("error", "No products selected.");
            return "redirect:/requester/products";
        }

        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/requester/products";
        }

        Demandes demande = new Demandes();
        demande.setDemandeur(user);
        demande.setEtat_demande(RequestStatus.PENDING);
        demande.setDate(java.time.LocalDate.now());

        for (Long productId : productIds) {
            String qtyStr = allRequestParams.get("quantities[" + productId + "]");
            int requestedQty;
            try {
                requestedQty = Integer.parseInt(qtyStr);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Invalid quantity for product ID " + productId);
                return "redirect:/requester/products";
            }

            Produits product = produitsRepository.findById(productId).orElse(null);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Product not found.");
                return "redirect:/requester/products";
            }
            if (requestedQty > product.getQuantite()) {
                redirectAttributes.addFlashAttribute("error", "Requested quantity for " + product.getNom() + " exceeds available stock.");
                return "redirect:/requester/products";
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
        return "redirect:/requester/products";
    }



}
