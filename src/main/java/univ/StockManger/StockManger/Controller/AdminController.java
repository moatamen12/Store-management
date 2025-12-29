package univ.StockManger.StockManger.Controller;

import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.Role;
import univ.StockManger.StockManger.entity.User;

import java.util.Locale;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model, @ModelAttribute("success") String success, @ModelAttribute("error") String error) {
        model.addAttribute("users", userRepository.findAll());
        if (success != null && !success.isEmpty()) model.addAttribute("success", success);
        if (error != null && !error.isEmpty()) model.addAttribute("error", error);
        return "admin";
    }

    @GetMapping("admin/addUser")
    public String addUser(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "admin_addUser";
    }

    @PostMapping("/admin/addUser")
    public String create(@Valid @ModelAttribute("user") User user, BindingResult result, RedirectAttributes redirectAttributes, Locale locale) {
        if (result.hasErrors()) {
            return "admin_addUser";
        }
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("user.create.success", null, locale));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("user.create.error", null, locale));
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/deleteUser/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes, Locale locale) {
        try {
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("user.delete.success", null, locale));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("user.delete.error", null, locale));
        }
        return "redirect:/admin";
    }

    @GetMapping("/admin/editUser/{id}")
    public String editUser(@PathVariable Long id, Model model, Locale locale) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("user.error.invalidId", new Object[]{id}, locale)));
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());
        return "admin_editeUser";
    }

    @PostMapping("/admin/editUser/{id}")
    public String updateUser(@PathVariable Long id, @Valid @ModelAttribute("user") User user, BindingResult result, RedirectAttributes redirectAttributes, Locale locale) {
        if (result.hasErrors()) {
            user.setId(id);
            return "admin_editeUser";
        }
        try {
            User existingUser = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("user.error.invalidId", new Object[]{id}, locale)));
            // Preserve the password if it's not being changed
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(existingUser.getPassword());
            } else {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("user.update.success", null, locale));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("user.update.error", null, locale));
        }
        return "redirect:/admin";
    }
}
