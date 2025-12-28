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
import java.util.Optional;

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
    public String showUpdateForm(@PathVariable("id") long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Produits> produit = produitsRepository.findById(id);
        if (produit.isPresent()) {
            model.addAttribute("produit", produit.get());
            return "ModifyProducte";
        } else {
            redirectAttributes.addFlashAttribute("error", "Invalid product Id:" + id);
            return "redirect:/stock";
        }
    }

    @PostMapping("/update/{id}")
    public String updateProduit(@PathVariable("id") long id, @ModelAttribute("produit") Produits produit, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isMagasinier = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MAGASINIER"));

            if (isMagasinier) {
                Produits existingProduit = produitsRepository.findById(id).orElse(null);
                if (existingProduit != null) {
                    existingProduit.setNom(produit.getNom());
                    existingProduit.setQuantite(produit.getQuantite());
                    existingProduit.setSeuilAlerte(produit.getSeuilAlerte());
                    existingProduit.setPrixUnitaire(produit.getPrixUnitaire());
                    existingProduit.setDescription(produit.getDescription());
                    
                    produitsRepository.save(existingProduit);
                    redirectAttributes.addFlashAttribute("success", "Product updated successfully.");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Product not found.");
                }
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
