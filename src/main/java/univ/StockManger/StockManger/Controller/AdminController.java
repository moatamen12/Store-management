package univ.StockManger.StockManger.Controller;


//import org.springframework.stereotype.Controller;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.User;

@Controller
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model) {
        model.addAttribute("users",userRepository.findAll());
        return "admin";
    }


    //adding a user - get
    @GetMapping("admin/addUser")
    public String addUser(Model model) {
        model.addAttribute("user",new User());
        return "admin_addUser";
    }

    @PostMapping("/admin/addUser")
    public String create(@Valid  @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "admin_addUser";
        }
        userRepository.save(user);
        return "redirect:/admin";
    }


/*
TODO
    removing a user
    Editing the users info
*/


}
