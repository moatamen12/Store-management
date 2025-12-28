package univ.StockManger.StockManger.Controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import univ.StockManger.StockManger.Repositories.ProduitsRepository;
import univ.StockManger.StockManger.entity.Produits;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Controller
public class StockController {

    private final ProduitsRepository produitsRepository;

    public StockController(ProduitsRepository produitsRepository) {
        this.produitsRepository = produitsRepository;
    }

    @GetMapping("/stock")
    public String stock(Model model, Authentication authentication,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String search,
                        Locale locale) {

//        model.addAttribute("locale", locale);

        if (authentication != null && authentication.isAuthenticated()) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            model.addAttribute("authorities", authorities);

            boolean isRequester = authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_DEMANDEUR"));

            if (isRequester) {
                Pageable pageable = PageRequest.of(page, size);
                Page<Produits> productPage;
                if (search != null && !search.isEmpty()) {
                    productPage = produitsRepository.findByNomContainingIgnoreCase(search, pageable);
                } else {
                    productPage = produitsRepository.findAll(pageable);
                }
                model.addAttribute("products", productPage.getContent());
                model.addAttribute("currentPage", productPage.getNumber());
                model.addAttribute("totalPages", productPage.getTotalPages());
                model.addAttribute("search", search);
            } else {
                List<Produits> products = produitsRepository.findAll();
                model.addAttribute("products", products);
            }
        }

        return "stock";
    }
}
