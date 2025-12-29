package univ.StockManger.StockManger.Controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import univ.StockManger.StockManger.Repositories.ProduitsRepository;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.Produits;
import univ.StockManger.StockManger.entity.RequestStatus;
import univ.StockManger.StockManger.entity.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Controller
public class StockController {

    private final ProduitsRepository produitsRepository;
    private final UserRepository userRepository;

    public StockController(ProduitsRepository produitsRepository, UserRepository userRepository) {
        this.produitsRepository = produitsRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/stock")
    public String stock(Model model, Authentication authentication,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) boolean lowStock,
                        Locale locale) {

        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User currentUser = userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            model.addAttribute("currentUser", currentUser);
            if (currentUser != null) {
                model.addAttribute("currentUserId", currentUser.getId());
            }

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            model.addAttribute("authorities", authorities);

            Pageable pageable = PageRequest.of(page, size);
            Page<Produits> productPage;

            if (lowStock) {
                productPage = produitsRepository.findLowStock(pageable);
            } else if (search != null && !search.isEmpty()) {
                Long searchLong = null;
                try {
                    searchLong = Long.parseLong(search);
                } catch (NumberFormatException e) {
                    // Ignore if search is not a valid Long
                }
                productPage = produitsRepository.findByNomOrId(search, searchLong, pageable);
            } else {
                productPage = produitsRepository.findAll(pageable);
            }

            List<Long> productsWithActiveRequests = productPage.getContent().stream()
                .filter(p -> produitsRepository.countActiveRequestsForProductInStatus(p.getId(), Arrays.asList(RequestStatus.PENDING, RequestStatus.APPROVED)) > 0)
                .map(Produits::getId)
                .collect(Collectors.toList());

            model.addAttribute("products", productPage.getContent());
            model.addAttribute("currentPage", productPage.getNumber());
            model.addAttribute("totalPages", productPage.getTotalPages());
            model.addAttribute("search", search);
            model.addAttribute("lowStock", lowStock);
            model.addAttribute("size", size);
            model.addAttribute("productsWithActiveRequests", productsWithActiveRequests);
        }

        return "stock";
    }
}
