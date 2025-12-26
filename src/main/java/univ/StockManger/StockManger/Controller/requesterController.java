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
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") int quantity,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        Produits product = produitsRepository.findById(productId).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Product not found.");
            return "redirect:/requester/products";
        }
        if (quantity > product.getQuantite()) {
            redirectAttributes.addFlashAttribute("error", "Requested quantity exceeds available stock.");
            return "redirect:/requester/products";
        }
        // Subtract requested quantity from stock
        product.setQuantite(product.getQuantite() - quantity);
        produitsRepository.save(product);

        // Find the current user
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/requester/products";
        }

        // Create and save the Demandes entity (adjust fields as needed)
        Demandes demande = new Demandes();
        demande.setDemandeur(user);
        demande.setEtat_demande(RequestStatus.PENDING); // or your default status
        demande.setDate(java.time.LocalDate.now());
        LigneDemande ligne = new LigneDemande();
        ligne.setProduit(product);
        ligne.setQuantiteDemandee(quantity);
        ligne.setDemande(demande);
        demande.getLignes().add(ligne);

        demandesRepository.save(demande);

        redirectAttributes.addFlashAttribute("success", "Request submitted successfully.");
        return "redirect:/requester/products";
    }
}
