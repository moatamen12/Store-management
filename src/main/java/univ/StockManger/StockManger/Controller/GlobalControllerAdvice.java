package univ.StockManger.StockManger.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import univ.StockManger.StockManger.Repositories.UserRepository;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    @Autowired
    public GlobalControllerAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void addAttributes(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            userRepository.findByEmail(authentication.getName()).ifPresent(user -> {
                model.addAttribute("currentUserId", user.getId());
                model.addAttribute("currentUser", user);
            });
        }
    }
}
