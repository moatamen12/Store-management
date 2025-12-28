package univ.StockManger.StockManger.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.StockManger.StockManger.Repositories.DemandesRepository;
import univ.StockManger.StockManger.Repositories.ProduitsRepository;
import univ.StockManger.StockManger.entity.Demandes;
import univ.StockManger.StockManger.entity.RequestStatus;

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
        // Data for "Requests to Process" tab
        List<Demandes> acceptedRequests = demandesRepository.findTop10ByEtatDemandeOrderByRequest_dateDesc(RequestStatus.APPROVED, PageRequest.of(0, 10));
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

//        // Data for "Stock Management" tab
//        model.addAttribute("products", produitsRepository.findAll());

        return "magasinier";
    }

    @GetMapping("/produit/delete/{id}")
    public String deleteProduit(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            produitsRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting product.");
        }
        return "redirect:/stock";
    }
}
