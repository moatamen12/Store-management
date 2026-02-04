package univ.StockManger.StockManger.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.StockManger.StockManger.Repositories.BonRepository;
import univ.StockManger.StockManger.Repositories.LigneBonRepository;
import univ.StockManger.StockManger.Repositories.ProduitsRepository;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.*;
import univ.StockManger.StockManger.events.NotificationType;
import univ.StockManger.StockManger.service.DatabaseUserDetailsService;
import univ.StockManger.StockManger.service.NotificationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/produits")
public class ProduitController {

    @Autowired
    private ProduitsRepository produitsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private BonRepository bonRepository;
    @Autowired
    private LigneBonRepository ligneBonRepository;
    @Autowired
    private MessageSource messageSource;

    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("produit", new Produits());
        return "addProducte";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute("produit") Produits produit, @RequestParam("quantite") int quantite, @RequestParam("pdf") MultipartFile pdf, RedirectAttributes redirectAttributes, Authentication authentication, Locale locale) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isMagasinier = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MAGASINIER"));

            if (isMagasinier) {
                if (pdf.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("bon.error.uploadPdf", null, locale));
                    return "redirect:/produits/add";
                }

                try {
                    // create the uploads directory if it doesn't exist
                    Path uploadPath = Paths.get(UPLOAD_DIR);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    DatabaseUserDetailsService.CustomUserDetails userDetails = (DatabaseUserDetailsService.CustomUserDetails) authentication.getPrincipal();
                    User magasinier = userDetails.getUser();

                    String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    String fileName = date + "_" + magasinier.getNom() + "_" + produit.getNom() + ".pdf";
                    Path path = Paths.get(UPLOAD_DIR + fileName);
                    Files.write(path, pdf.getBytes());

                    produit.setCreatedBy(authentication.getName());
                    produit.setQuantite(quantite);
                    produitsRepository.save(produit);

                    Bon bon = new Bon();
                    bon.setDate(LocalDate.now());
                    bon.setMagasinier(magasinier);
                    bon.setType(ReceiptType.ENTRY);
                    bon.setPdfPath(fileName);
                    bonRepository.save(bon);

                    LigneBon ligneBon = new LigneBon();
                    ligneBon.setBon(bon);
                    ligneBon.setProduit(produit);
                    ligneBon.setQuantite(quantite);
                    ligneBonRepository.save(ligneBon);

                    checkLowStock(produit, locale);
                    redirectAttributes.addFlashAttribute("success", messageSource.getMessage("product.add.success", null, locale));
                    return "redirect:/produits/add";
                } catch (IOException e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("bon.error.failedToUpload", null, locale));
                }
            } else {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("auth.error.notAuthorized", null, locale));
            }
        } else {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("auth.error.mustBeLoggedIn", null, locale));
        }
        return "redirect:/login";
    }

    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable("id") long id, Model model, RedirectAttributes redirectAttributes, Locale locale) {
        Optional<Produits> produit = produitsRepository.findById(id);
        if (produit.isPresent()) {
            model.addAttribute("produit", produit.get());
            return "ModifyProducte";
        } else {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("product.error.invalidId", new Object[]{id}, locale));
            return "redirect:/stock";
        }
    }

    @PostMapping("/update/{id}")
    public String updateProduit(@PathVariable("id") long id, @ModelAttribute("produit") Produits produit, RedirectAttributes redirectAttributes, Authentication authentication, Locale locale) {
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
                    checkLowStock(existingProduit, locale);
                    redirectAttributes.addFlashAttribute("success", messageSource.getMessage("product.update.success", null, locale));
                } else {
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("product.error.notFound", null, locale));
                }
            } else {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("auth.error.notAuthorized", null, locale));
            }
        } else {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("auth.error.mustBeLoggedIn", null, locale));
        }
        return "redirect:/stock";
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes, Authentication authentication, Locale locale) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isMagasinier = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_MAGASINIER"));

            if (isMagasinier) {
                if (produitsRepository.countActiveRequestsForProductInStatus(id, Collections.singletonList(RequestStatus.PENDING)) > 0) {
                    redirectAttributes.addFlashAttribute("error", messageSource.getMessage("product.delete.error.pendingRequest", null, locale));
                } else {
                    produitsRepository.deleteById(id);
                    redirectAttributes.addFlashAttribute("success", messageSource.getMessage("product.delete.success", null, locale));
                }
            } else {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("auth.error.notAuthorized", null, locale));
            }
        } else {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("auth.error.mustBeLoggedIn", null, locale));
        }
        return "redirect:/stock";
    }

    private void checkLowStock(Produits produit, Locale locale) {
        if (produit.getQuantite() <= produit.getSeuilAlerte()) {
            List<User> magasiniers = userRepository.findAllByRole(Role.magasinier);
            for (User magasinier : magasiniers) {
                notificationService.createNotification(this, NotificationType.LOW_STOCK,
                        messageSource.getMessage("notification.lowStock", new Object[]{produit.getNom()}, locale),
                        produit.getId(), magasinier.getId());
            }
        }
    }
}
