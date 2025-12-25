package univ.StockManger.StockManger.Controller;


import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.User;

import java.security.Principal;

@Controller
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //geting the current loggedin user
    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }


    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }



    @GetMapping("/admin")
    public String adminDashboard(Model model) {
//        String username = (principal != null) ? principal.getName() : "Guest";
//        model.addAttribute("user", currentUser());
        model.addAttribute("currentUser", currentUser().getPrenom() + " " + currentUser().getNom());
        model.addAttribute("users",userRepository.findAll());

        return "admin";
    }


    //adding a user - get
    @GetMapping("admin/addUser")
    public String addUser(Model model) {
        model.addAttribute("user",new User());
        return "admin_addUser";
    }

    //adding a user - post
    @PostMapping("/admin/addUser")
    public String create(@Valid  @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "admin_addUser";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));  // Uncomment and use this
        userRepository.save(user);
        return "redirect:/admin";
    }

    //delete a user
    @DeleteMapping("/admin/deleteUser/{id}")
    public String deleteUser(@ModelAttribute("id") Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin";
    }

    //editing user info - get
    @GetMapping("/admin/editUser/{id}")
    public String editUser(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        return "admin_editeUser";
    }

    //editing user info - post
    @PostMapping("/admin/editUser/{id}")
    public String updateUser(@PathVariable Long id, @Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            user.setId(id);
            return "admin_editeUser";
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            User existingUser = userRepository.findById(id).orElseThrow();
            user.setPassword(existingUser.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));  // Uncomment and use this
        }
        userRepository.save(user);
        return "redirect:/admin";
    }



/*
TODO
    removing a user -don
    Editing the users info
    searchign by ID/Name/role/Email
*/


}
