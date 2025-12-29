package univ.StockManger.StockManger.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import univ.StockManger.StockManger.Repositories.BonRepository;
import univ.StockManger.StockManger.Repositories.DemandesRepository;
import univ.StockManger.StockManger.Repositories.LigneBonRepository;
import univ.StockManger.StockManger.Repositories.ProduitsRepository;
import univ.StockManger.StockManger.entity.*;
import univ.StockManger.StockManger.events.NotificationType;
import univ.StockManger.StockManger.service.BonSpecification;
import univ.StockManger.StockManger.service.DatabaseUserDetailsService;
import univ.StockManger.StockManger.service.NotificationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class MagasinierController {

    @Autowired
    private DemandesRepository demandesRepository;
    @Autowired
    private ProduitsRepository produitsRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private BonRepository bonRepository;
    @Autowired
    private LigneBonRepository ligneBonRepository;
    @Autowired
    private MessageSource messageSource;

    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping("/magasinier")
    public String dashboard(Model model, Locale locale) {
        List<RequestStatus> statuses = Arrays.asList(RequestStatus.APPROVED, RequestStatus.PENDING);
        List<Demandes> acceptedRequests = demandesRepository.findTop10ByEtat_demandeInOrderByRequest_dateDesc(statuses, PageRequest.of(0, 10));
        List<Map<String, Object>> requestViews = acceptedRequests.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("demande", d);
            String userName = (d.getDemandeur() != null)
                    ? d.getDemandeur().getNom() + " " + d.getDemandeur().getPrenom()
                    : messageSource.getMessage("user.deleted", null, locale);
            map.put("demandeurName", userName);
            return map;
        }).collect(Collectors.toList());
        model.addAttribute("requests", requestViews);

        return "magasinier";
    }

    @GetMapping("/magasinier/requests")
    public String requestsList(Model model,
                               @RequestParam(required = false) String success,
                               @RequestParam(required = false) String error,
                               Locale locale) {
        if (success != null) {
            model.addAttribute("success", success);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }

        DatabaseUserDetailsService.CustomUserDetails userDetails = (DatabaseUserDetailsService.CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User magasinier = userDetails.getUser();
        List<Demandes> allRequests = demandesRepository.findMagasinierRequests(magasinier);

        List<Map<String, Object>> requestViews = allRequests.stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("demande", d);
            String userName = (d.getDemandeur() != null)
                    ? d.getDemandeur().getNom() + " " + d.getDemandeur().getPrenom()
                    : messageSource.getMessage("user.deleted", null, locale);
            map.put("demandeurName", userName);
            return map;
        }).collect(Collectors.toList());
        model.addAttribute("requests", requestViews);

        return "magasinier_requests";
    }

    @PostMapping("/magasinier/request/deliver/{id}")
    public String deliverRequest(@PathVariable Long id, RedirectAttributes redirectAttributes, Locale locale) {
        Demandes demande = demandesRepository.findById(id).orElse(null);
        if (demande == null) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.notFound", null, locale));
            return "redirect:/magasinier";
        }

        if (demande.getEtat_demande() != RequestStatus.APPROVED) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.onlyApprovedCanBeDelivered", null, locale));
            return "redirect:/magasinier";
        }

        for (LigneDemande ligne : demande.getLignes()) {
            Produits produit = ligne.getProduit();
            int quantiteDemandee = ligne.getQuantiteDemandee();
            if (produit.getQuantite() < quantiteDemandee) {
                redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.insufficientStock", new Object[]{produit.getNom()}, locale));
                return "redirect:/magasinier";
            }
        }

        DatabaseUserDetailsService.CustomUserDetails userDetails = (DatabaseUserDetailsService.CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User magasinier = userDetails.getUser();

        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String requesterName = demande.getDemandeur().getNom();
        String fileName = date + "_" + requesterName + "_" + magasinier.getNom() + ".pdf";

        Bon bon = new Bon();
        bon.setDate(LocalDate.now());
        bon.setDemande(demande);
        bon.setMagasinier(magasinier);
        bon.setType(ReceiptType.EXIT);
        bon.setPdfPath(fileName);
        bon = bonRepository.save(bon);

        List<LigneBon> lignesBon = new ArrayList<>();
        for (LigneDemande ligne : demande.getLignes()) {
            Produits produit = ligne.getProduit();
            int quantiteDemandee = ligne.getQuantiteDemandee();
            produit.setQuantite(produit.getQuantite() - quantiteDemandee);
            produitsRepository.save(produit);

            LigneBon ligneBon = new LigneBon();
            ligneBon.setBon(bon);
            ligneBon.setProduit(produit);
            ligneBon.setQuantite(quantiteDemandee);
            lignesBon.add(ligneBonRepository.save(ligneBon));
        }
        bon.setLignesBon(lignesBon);


        demande.setEtat_demande(RequestStatus.DELIVERED);
        demandesRepository.save(demande);

        // Notify the original requester that their request has been delivered
        if (demande.getDemandeur() != null) {
            notificationService.createNotification(this, NotificationType.REQUEST_DELIVERED,
                    messageSource.getMessage("notification.request.delivered", new Object[]{demande.getId()}, locale),
                    demande.getId(), demande.getDemandeur().getId());
        }

        redirectAttributes.addFlashAttribute("success", messageSource.getMessage("request.success.delivered", null, locale));
        return "redirect:/magasinier/requests";
    }

    @GetMapping("/magasinier/bon-entree")
    public String showBonEntreeForm(Model model) {
        model.addAttribute("produits", produitsRepository.findAll());
        return "bon_entree";
    }

    @PostMapping("/magasinier/bon-entree")
    public String createBonEntree(@RequestParam Long produitId, @RequestParam int quantite, @RequestParam("pdf") MultipartFile pdf, RedirectAttributes redirectAttributes, Locale locale) {
        Produits produit = produitsRepository.findById(produitId).orElse(null);
        if (produit == null) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("request.error.productNotFound", null, locale));
            return "redirect:/magasinier/bon-entree";
        }

        if (pdf.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("bon.error.uploadPdf", null, locale));
            return "redirect:/magasinier/bon-entree";
        }

        try {
            // Create the uploads directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            DatabaseUserDetailsService.CustomUserDetails userDetails = (DatabaseUserDetailsService.CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User magasinier = userDetails.getUser();

            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String fileName = date + "_" + magasinier.getNom() + "_" + produit.getNom() + ".pdf";
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.write(path, pdf.getBytes());

            Bon bon = new Bon();
            bon.setDate(LocalDate.now());
            bon.setMagasinier(magasinier);
            bon.setType(ReceiptType.ENTRY);
            bon.setPdfPath(fileName);
            bon = bonRepository.save(bon);

            LigneBon ligneBon = new LigneBon();
            ligneBon.setBon(bon);
            ligneBon.setProduit(produit);
            ligneBon.setQuantite(quantite);
            ligneBonRepository.save(ligneBon);

            produit.setQuantite(produit.getQuantite() + quantite);
            produitsRepository.save(produit);

            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("stock.success.updated", null, locale));
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", messageSource.getMessage("bon.error.failedToUpload", null, locale));
        }

        return "redirect:/magasinier/bon-entree";
    }

    @GetMapping("/magasinier/bons")
    public String listBons(Model model, @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("date").descending());
        Specification<Bon> spec = BonSpecification.search(keyword);
        Page<Bon> bonPage = bonRepository.findAll(spec, pageable);

        model.addAttribute("bons", bonPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bonPage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "bons";
    }
}
