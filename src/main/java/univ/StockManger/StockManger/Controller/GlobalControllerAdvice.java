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
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<User> u = userRepository.findByEmail(email);
            if (u.isPresent()) {
                User user = u.get();
                model.addAttribute("currentUser", user.getPrenom() + " " + user.getNom());
            } else {
                model.addAttribute("currentUser", "Guest");
            }
        } catch (Exception e) {
            model.addAttribute("currentUser", "Guest");
        }
    }
}
