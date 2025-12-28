package univ.StockManger.StockManger.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.StockManger.StockManger.Repositories.DemandesRepository;
import univ.StockManger.StockManger.Repositories.ProduitsRepository;
import univ.StockManger.StockManger.entity.Demandes;
import univ.StockManger.StockManger.entity.RequestStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class MagasinierController {

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private ProduitsRepository produitsRepository;

    @GetMapping("/magasinier")
    public String dashboard(Model model) {
        List<RequestStatus> statuses = Arrays.asList(RequestStatus.APPROVED, RequestStatus.PENDING);
        List<Demandes> acceptedRequests = demandesRepository.findTop10ByEtat_demandeInOrderByRequest_dateDesc(statuses, PageRequest.of(0, 10));
        List<Map<String, Object>> requestViews = acceptedRequests.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("demande", d);
            String userName = (d.getDemandeur() != null)
                    ? d.getDemandeur().getNom() + " " + d.getDemandeur().getPrenom()
                    : "deleted user";
            map.put("demandeurName", userName);
            return map;
        }).collect(Collectors.toList());
        model.addAttribute("requests", requestViews);

        return "magasinier";
    }

    @GetMapping("/magasinier/requests")
    public String requestsList(Model model,
                               @RequestParam(required = false) String success,
                               @RequestParam(required = false) String error) {
        if (success != null) {
            model.addAttribute("success", success);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }

        List<RequestStatus> statuses = Arrays.asList(RequestStatus.APPROVED, RequestStatus.PENDING);
        List<Demandes> allRequests = demandesRepository.findByEtat_demandeInOrderByRequest_dateDesc(statuses);
        List<Map<String, Object>> requestViews = allRequests.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("demande", d);
            String userName = (d.getDemandeur() != null)
                    ? d.getDemandeur().getNom() + " " + d.getDemandeur().getPrenom()
                    : "deleted user";
            map.put("demandeurName", userName);
            return map;
        }).collect(Collectors.toList());
        model.addAttribute("requests", requestViews);
        return "magasinier_requests";
    }

    @PostMapping("/magasinier/request/deliver/{id}")
    public String deliverRequest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Demandes demande = demandesRepository.findById(id).orElse(null);
        if (demande == null) {
            redirectAttributes.addFlashAttribute("error", "Request not found.");
            return "redirect:/magasinier/requests";
        }

        if (demande.getEtat_demande() != RequestStatus.APPROVED) {
            redirectAttributes.addFlashAttribute("error", "Only approved requests can be marked as delivered.");
            return "redirect:/magasinier/requests";
        }

        demande.setEtat_demande(RequestStatus.DELIVERED);
        demandesRepository.save(demande);
        redirectAttributes.addFlashAttribute("success", "Request marked as delivered.");
        return "redirect:/magasinier/requests";
    }
}
