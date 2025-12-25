package univ.StockManger.StockManger.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.StockManger.StockManger.Repositories.DemandesRepository;
import univ.StockManger.StockManger.entity.Demandes;
import univ.StockManger.StockManger.entity.RequestStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
//@RequestMapping("/sg")
public class SGController {

    //adding the dependeses
//    private final UserRepository userRepository;
    private final DemandesRepository requistRrpository;

    public SGController(DemandesRepository requistRrpository) {
//        this.userRepository = userRepository;
        this.requistRrpository = requistRrpository;
    }

    //get the requist list
    @GetMapping("/sg")
    public String sgDashboard(Model model){
//        model.addAttribute("requests",requistRrpository.findAll());
        List<Demandes> requests = requistRrpository.findAll();
        requests.sort(Comparator.comparing(d->d.getEtat_demande() != RequestStatus.PENDING));
        model.addAttribute("requests", requests);
        return "sg";
    }

    //update requist status
    //approve requist
    @PostMapping("/sg/update/{id}/approve")
    public String approveRequest(@PathVariable Long id, RedirectAttributes redirectAttributes){
        Optional<Demandes> opt = requistRrpository.findById(id);
        if(opt.isPresent()){
            Demandes demande = opt.get();
            demande.setEtat_demande(RequestStatus.APPROVED);
            requistRrpository.save(demande);
            redirectAttributes.addFlashAttribute("success", "Request approved successfully.");
        }else{
            redirectAttributes.addFlashAttribute("error", "Request not approved.");
        }
        return "redirect:/sg";
    }
    //reject requist
    @PostMapping("/sg/update/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                                @RequestParam(required = false, name = "commentaire") String commentaire,
                                RedirectAttributes redirectAttributes){
        Optional<Demandes> opt = requistRrpository.findById(id);
        if(opt.isPresent()){
            Demandes demande = opt.get();
            demande.setEtat_demande(RequestStatus.REJECTED);
            if(commentaire != null && !commentaire.trim().isEmpty()){
                demande.setCommentaire(commentaire);
            }
            requistRrpository.save(demande);
            redirectAttributes.addFlashAttribute("success", "Request rejected successfully.");
        }else{
            redirectAttributes.addFlashAttribute("error", "Request not rejected.");
        }
        return "redirect:/sg";
    }


    //sgDashboard
    /*TODO
    * display al requists
    * validateRequest
    * generateReports
    *
    *
    * */
}
