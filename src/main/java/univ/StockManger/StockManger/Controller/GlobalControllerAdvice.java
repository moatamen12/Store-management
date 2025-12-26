package univ.StockManger.StockManger.Controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.User;

import java.util.Optional;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    public GlobalControllerAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void addCurrentUser(Model model) {
        try {
            String email = null;
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                email = SecurityContextHolder.getContext().getAuthentication().getName();
            }
            if (email != null) {
                Optional<User> u = userRepository.findByEmail(email);
                if (u.isPresent()) {
                    User user = u.get();
                    model.addAttribute("currentUser", user.getPrenom() + " " + user.getNom());
                    model.addAttribute("currentUserEmail", user.getEmail());
                    return;
                }
            }
            model.addAttribute("currentUser", "Guest");
            model.addAttribute("currentUserEmail", "");
        } catch (Exception e) {
            model.addAttribute("currentUser", "Guest");
            model.addAttribute("currentUserEmail", "");
        }
    }
}
