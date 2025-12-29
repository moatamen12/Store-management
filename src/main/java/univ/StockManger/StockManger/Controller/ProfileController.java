// java
package univ.StockManger.StockManger.Controller;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.User;

import java.util.Locale;

@Controller
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
    }

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    @GetMapping("/profile")
    public String profileView(Model model) {
        model.addAttribute("profileUser", currentUser());
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String profileEditForm(Model model) {
        model.addAttribute("user", currentUser());
        return "profile_edit";
    }

    @PostMapping("/profile/edit")
    public String profileEditSubmit(@ModelAttribute User formUser,
                                    @RequestParam(value = "currentPassword", required = false) String currentPassword,
                                    Model model,
                                    RedirectAttributes redirectAttributes,
                                    Locale locale) {
        User user = currentUser();

        // email uniqueness check if changed
        if (!user.getEmail().equals(formUser.getEmail()) && userRepository.existsByEmail(formUser.getEmail())) {
            model.addAttribute("user", formUser);
            model.addAttribute("emailError", messageSource.getMessage("profile.error.emailInUse", null, locale));
            model.addAttribute("error", messageSource.getMessage("profile.error.updateFailed", null, locale));
            return "profile_edit";
        }

        user.setNom(formUser.getNom());
        user.setPrenom(formUser.getPrenom());
        user.setEmail(formUser.getEmail());

        // update password only when provided (non-empty)
        if (formUser.getPassword() != null && !formUser.getPassword().isBlank()) {
            // require current password and validate it
            if (currentPassword == null || currentPassword.isBlank() || !passwordEncoder.matches(currentPassword, user.getPassword())) {
                model.addAttribute("user", formUser);
                model.addAttribute("passwordError", messageSource.getMessage("profile.error.incorrectPassword", null, locale));
                model.addAttribute("error", messageSource.getMessage("profile.error.updateFailed", null, locale));
                // clear sensitive fields before redisplay
                formUser.setPassword(null);
                return "profile_edit";
            }
            user.setPassword(passwordEncoder.encode(formUser.getPassword()));
        }

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("profile.success.updated", null, locale));
        return "redirect:/profile";
    }
}
