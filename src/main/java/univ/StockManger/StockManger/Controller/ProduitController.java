package univ.StockManger.StockManger.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.StockManger.StockManger.Repositories.ProduitsRepository;
import univ.StockManger.StockManger.entity.Produits;
import univ.StockManger.StockManger.entity.RequestStatus;

import java.util.Collections;

@Controller
@RequestMapping("/produits")
public class ProduitController {

    @Autowired
    private ProduitsRepository produitsRepository;

    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("produit", new Produits());
        return "addProducte";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute("produit") Produits produit, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isMagasinier = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MAGASINIER"));

            if (isMagasinier) {
                produit.setCreatedBy(authentication.getName());
                produitsRepository.save(produit);
                redirectAttributes.addFlashAttribute("success", "Product added successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to add products.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to perform this action.");
        }
        return "redirect:/stock";
    }

    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
        Produits produit = produitsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("produit", produit);
        return "ModifyProducte";
    }

    @PostMapping("/update/{id}")
    public String updateProduit(@PathVariable("id") long id, @ModelAttribute("produit") Produits produit, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isMagasinier = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MAGASINIER"));

            if (isMagasinier) {
                produitsRepository.save(produit);
                redirectAttributes.addFlashAttribute("success", "Product updated successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to update products.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to perform this action.");
        }
        return "redirect:/stock";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isMagasinier = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MAGASINIER"));

            if (isMagasinier) {
                if (produitsRepository.countActiveRequestsForProductInStatus(id, Collections.singletonList(RequestStatus.PENDING)) > 0) {
                    redirectAttributes.addFlashAttribute("error", "Cannot delete product because it is part of a pending request.");
                } else {
                    produitsRepository.deleteById(id);
                    redirectAttributes.addFlashAttribute("success", "Product deleted successfully.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to delete products.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to perform this action.");
        }
        return "redirect:/stock";
    }
}
