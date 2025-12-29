package univ.StockManger.StockManger.Controller;

import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import univ.StockManger.StockManger.Repositories.BonRepository;
import univ.StockManger.StockManger.entity.Bon;
import univ.StockManger.StockManger.service.PdfService;

import java.io.IOException;

@Controller
public class BonController {

    @Autowired
    private BonRepository bonRepository;

    @Autowired
    private PdfService pdfService;

    @GetMapping("/bon/{id}/pdf")
    public ResponseEntity<byte[]> downloadBonPdf(@PathVariable Long id) throws DocumentException, IOException {
        Bon bon = bonRepository.findById(id).orElse(null);
        if (bon == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdf = pdfService.generateBonPdf(bon);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + bon.getPdfPath() + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/bon/{id}/view")
    public ResponseEntity<byte[]> viewBonPdf(@PathVariable Long id) throws DocumentException, IOException {
        Bon bon = bonRepository.findById(id).orElse(null);
        if (bon == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdf = pdfService.generateBonPdf(bon);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
