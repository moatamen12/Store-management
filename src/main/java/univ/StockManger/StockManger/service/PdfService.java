package univ.StockManger.StockManger.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import univ.StockManger.StockManger.entity.Bon;
import univ.StockManger.StockManger.entity.LigneBon;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] generateBonPdf(Bon bon) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);

        document.open();

        document.add(new Paragraph("Bon de " + bon.getType().toString()));
        document.add(new Paragraph("Date: " + bon.getDate().toString()));
        document.add(new Paragraph("Numero Bon: " + bon.getId()));
        if (bon.getDemande() != null) {
            document.add(new Paragraph("Numero Demande: " + bon.getDemande().getId()));
        }
        document.add(new Paragraph("Magasinier: " + bon.getMagasinier().getNom() + " " + bon.getMagasinier().getPrenom()));
        document.add(new Paragraph(" "));

        for (LigneBon ligne : bon.getLignesBon()) {
            document.add(new Paragraph(
                    "Produit: " + ligne.getProduit().getNom() +
                            ", Quantite: " + ligne.getQuantite()
            ));
        }

        document.close();
        return baos.toByteArray();
    }
}
