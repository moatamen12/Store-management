package univ.StockManger.StockManger.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import univ.StockManger.StockManger.Repositories.DemandesRepository;
import univ.StockManger.StockManger.entity.Demandes;
import univ.StockManger.StockManger.entity.RequestStatus;

import java.util.Arrays;
import java.util.List;

@Controller
public class MagasinierController {

    @Autowired
    private DemandesRepository demandesRepository;

    @GetMapping("/magasinier")
    public String dashboard(Model model) {
        List<Demandes> allRequests = demandesRepository.findAll();
        model.addAttribute("requests", allRequests);
        model.addAttribute("statuses", Arrays.asList(RequestStatus.values()));
        return "magasinier_dashboard";
    }

    @GetMapping("/magasinier/requests")
    public String getRequestsByStatus(@RequestParam(required = false) RequestStatus status, Model model) {
        List<Demandes> requests;
        if (status != null) {
            requests = demandesRepository.findByEtatDemande(status);
        } else {
            requests = demandesRepository.findAll();
        }
        model.addAttribute("requests", requests);
        model.addAttribute("statuses", Arrays.asList(RequestStatus.values()));
        return "magasinier_dashboard";
    }
}
