package univ.StockManger.StockManger.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.StockManger.StockManger.Repositories.DemandesRepository;
import univ.StockManger.StockManger.Repositories.ProduitsRepository;
import univ.StockManger.StockManger.Repositories.UserRepository;
import univ.StockManger.StockManger.entity.*;
import univ.StockManger.StockManger.events.NotificationType;
import univ.StockManger.StockManger.service.NotificationService;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class requesterController {

    @Autowired
    private ProduitsRepository produitsRepository;
    @Autowired
    private DemandesRepository demandesRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private MessageSource messageSource;

    @GetMapping("/requester")
    public String requesterDashboard(Model model, Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            List<Demandes> requests = demandesRepository.findTop10RecentForDemandeur(user.getId(), PageRequest.of(0, 10));
            requests.sort(Comparator.comparing((Demandes d) -> d.getEtat_demande() != RequestStatus.PENDING)
                    .thenComparing(Demandes::getRequest_date, Comparator.reverseOrder()));
            model.addAttribute("requests", requests);

            long totalRequests = demandesRepository.countByDemandeurId(user.getId());
            model.addAttribute("pendingCount", demandesRepository.countByDemandeurIdAndEtat_demande(user.getId(), RequestStatus.PENDING));
            model.addAttribute("approvedCount", demandesRepository.countByDemandeurIdAndEtat_demande(user.getId(), RequestStatus.APPROVED));
            model.addAttribute("rejectedCount", demandesRepository.countByDemandeurIdAndEtat_demande(user.getId(), RequestStatus.REJECTED));
            model.addAttribute("deliveredCount", demandesRepository.countByDemandeurIdAndEtat_demande(user.getId(), RequestStatus.DELIVERED));
        }
        return "requester";
    }

    @GetMapping("/requester/requests")
    public String myRequests(Model model, Principal principal,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String search) {
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Demandes> requestPage;
            if (search != null && !search.isEmpty()) {
                requestPage = demandesRepository.findByDemandeurIdAndLignesProduitNomContainingIgnoreCaseOrderByRequest_dateDesc(user.getId(), search, pageable);
            } else {
                requestPage = demandesRepository.findByDemandeurIdOrderByRequest_dateDesc(user.getId(), pageable);
            }

            List<Demandes> sortedRequests = requestPage.getContent().stream()
                    .sorted(Comparator.comparing((Demandes d) -> d.getEtat_demande() != RequestStatus.PENDING)
                            .thenComparing(Demandes::getRequest_date, Comparator.reverseOrder()))
                    .collect(Collectors.toList());

            Page<Demandes> sortedPage = new PageImpl<>(sortedRequests, pageable, requestPage.getTotalElements());

            model.addAttribute("requests", sortedPage.getContent());
            model.addAttribute("currentPage", sortedPage.getNumber());
            model.addAttribute("totalPages", sortedPage.getTotalPages());
            model.addAttribute("search", search);
        }
        return "requister_Requests";
    }

    @PostMapping("/requester/request")
    public String submitRequest(
            @RequestParam(value = "selectedProducts", required = false) Long[] productIds,
            @RequestParam Map<String, String> allRequestParams,
            Principal principal,
            RedirectAttributes redirectAttributes,
            Locale locale) {

        if (productIds == null || productIds.length == 0) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.noProductsSelected", null, locale));
            return "redirect:/stock";
        }

        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.userNotFound", null, locale));
            return "redirect:/stock";
        }

        Demandes demande = new Demandes();
        demande.setDemandeur(user);
        demande.setEtat_demande(RequestStatus.PENDING);
        demande.setRequest_date(java.time.LocalDate.now());

        for (Long productId : productIds) {
            String qtyStr = allRequestParams.get("quantities[" + productId + "]");
            int requestedQty;
            try {
                requestedQty = Integer.parseInt(qtyStr);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.invalidQuantity", new Object[]{productId}, locale));
                return "redirect:/stock";
            }

            Produits product = produitsRepository.findById(productId).orElse(null);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.productNotFound", null, locale));
                return "redirect:/stock";
            }
            if (requestedQty > product.getQuantite()) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.quantityExceedsStock", new Object[]{product.getNom()}, locale));
                return "redirect:/stock";
            }

            LigneDemande ligne = new LigneDemande();
            ligne.setProduit(product);
            ligne.setQuantiteDemandee(requestedQty);
            ligne.setDemande(demande);
            demande.getLignes().add(ligne);
        }

        demandesRepository.save(demande);

        // Notify all Secretaries General
        List<User> secretaries = userRepository.findAllByRole(Role.Secretaire_General);
        for (User secretary : secretaries) {
            notificationService.createNotification(this, NotificationType.REQUEST_CREATED,
                    "New request #" + demande.getId() + " submitted by " + user.getNom(),
                    demande.getId(), secretary.getId());
        }

        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("request.success.submitted", null, locale));
        return "redirect:/stock";
    }

    @Transactional
    @GetMapping("/requester/request/cancel/{id}")
    public String cancelRequest(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes, Locale locale) {
        User user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.userNotFound", null, locale));
            return "redirect:/requester/requests";
        }

        Demandes demande = demandesRepository.findById(id).orElse(null);
        if (demande == null || !demande.getDemandeur().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.notFoundOrPermission", null, locale));
            return "redirect:/requester/requests";
        }

        if (demande.getEtat_demande() != RequestStatus.PENDING) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.onlyPendingCanBeCanceled", null, locale));
            return "redirect:/requester/requests";
        }

        demandesRepository.delete(demande);
        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("request.success.canceled", null, locale));
        return "redirect:/requester/requests";
    }
}
