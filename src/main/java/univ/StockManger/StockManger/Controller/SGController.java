// java
// File: `src/main/java/univ/StockManger/StockManger/Controller/SGController.java`
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
public class SGController {

    private final DemandesRepository demandesRepository;

    public SGController(DemandesRepository demandesRepository) {
        this.demandesRepository = demandesRepository;
    }

    @GetMapping("/sg")
    public String sgDashboard(Model model) {
        return "sg_requists";
    }

    // single handler for the requests page (removed duplicate mapping)
    @GetMapping("/sg/requests")
    public String sgRequests(Model model){
        List<Demandes> requests = demandesRepository.findAll();
        // put PENDING first (stable)
        requests.sort(Comparator.comparing(d -> d.getEtat_demande() != RequestStatus.PENDING));
        model.addAttribute("requests", requests);
        return "sg_requists";
    }

    @PostMapping("/sg/update/{id}/approve")
    public String approveRequest(@PathVariable Long id, RedirectAttributes redirectAttributes){
        Optional<Demandes> opt = demandesRepository.findById(id);
        if(opt.isPresent()){
            Demandes demande = opt.get();
            demande.setEtat_demande(RequestStatus.APPROVED);
            demandesRepository.save(demande);
            redirectAttributes.addFlashAttribute("success", "Request approved successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Request not approved.");
        }
        return "redirect:/sg/requests";
    }

    @PostMapping("/sg/update/{id}/reject")
    public String rejectRequest(@PathVariable Long id,
                                @RequestParam(required = false, name = "commentaire") String commentaire,
                                RedirectAttributes redirectAttributes){
        Optional<Demandes> opt = demandesRepository.findById(id);
        if(opt.isPresent()){
            Demandes demande = opt.get();
            demande.setEtat_demande(RequestStatus.REJECTED);
            if(commentaire != null && !commentaire.trim().isEmpty()){
                demande.setCommentaire(commentaire);
            }
            demandesRepository.save(demande);
            redirectAttributes.addFlashAttribute("success", "Request rejected successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Request not rejected.");
        }
        return "redirect:/sg/requests";
    }
}
