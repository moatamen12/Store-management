package univ.StockManger.StockManger.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import univ.StockManger.StockManger.Repositories.DemandesRepository;

@Controller
//@RequestMapping("/sg")
public class SGController {

    //adding the dependeses
//    private final UserRepository userRepository;
    private final DemandesRepository requistREpository;

    public SGController(DemandesRepository requistREpository) {
//        this.userRepository = userRepository;
        this.requistREpository = requistREpository;
    }

    //get the requist list
    @GetMapping("/sg")
    public String sgDashboard(Model model){
        model.addAttribute("requists",requistREpository.findAll());
        return "sg";
    }


    //sgDashboard
    /*TODO
    * validateRequest
    * generateReports
    *
    *
    * */
}
