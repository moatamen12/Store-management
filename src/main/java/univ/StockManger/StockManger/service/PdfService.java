package univ.StockManger.StockManger.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import univ.StockManger.StockManger.entity.Bon;
import univ.StockManger.StockManger.entity.LigneBon;
import univ.StockManger.StockManger.entity.ReceiptType;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class PdfService {

    public byte[] generateBonPdf(Bon bon) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();

        // Define fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.BLACK);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
        Font boldBodyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);

        // === Header Section ===
        String titleText = bon.getType() == ReceiptType.ENTRY ? "ENTRY VOUCHER" : "EXIT VOUCHER";
        Paragraph title = new Paragraph(titleText, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // Info table
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(20);
        infoTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        infoTable.addCell(new Phrase("Date: " + bon.getDate(), boldBodyFont));
        infoTable.addCell(new Phrase("Voucher No: " + bon.getId(), boldBodyFont));
        
        if (bon.getDemande() != null && bon.getDemande().getDemandeur() != null) {
            infoTable.addCell(new Phrase("Requester: " + bon.getDemande().getDemandeur().getNom() + " " + bon.getDemande().getDemandeur().getPrenom(), bodyFont));
        } else {
            infoTable.addCell(new Phrase(" ", bodyFont));
        }
        infoTable.addCell(new Phrase("Storekeeper: " + bon.getMagasinier().getNom() + " " + bon.getMagasinier().getPrenom(), bodyFont));
        
        document.add(infoTable);

        // === Table of Products ===
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 1, 2, 2});
        table.setSpacingBefore(10);

        // Table Header
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(74, 85, 104)); // Dark gray-blue
        cell.setPadding(8);
        
        cell.setPhrase(new Phrase("DESIGNATION", headerFont));
        table.addCell(cell);
        cell.setPhrase(new Phrase("QUANTITY", headerFont));
        table.addCell(cell);
        cell.setPhrase(new Phrase("UNIT PRICE", headerFont));
        table.addCell(cell);
        cell.setPhrase(new Phrase("TOTAL AMOUNT", headerFont));
        table.addCell(cell);

        // Table Body
        double totalAmount = 0;
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);

        for (LigneBon ligne : bon.getLignesBon()) {
            double montant = ligne.getQuantite() * ligne.getProduit().getPrixUnitaire();
            totalAmount += montant;

            table.addCell(new Phrase(ligne.getProduit().getNom(), bodyFont));
            table.addCell(new Phrase(String.valueOf(ligne.getQuantite()), bodyFont));
            table.addCell(new Phrase(currencyFormatter.format(ligne.getProduit().getPrixUnitaire()), bodyFont));
            table.addCell(new Phrase(currencyFormatter.format(montant), bodyFont));
        }
        document.add(table);

        // === Footer Section ===
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(100);
        totalTable.setSpacingBefore(20);
        totalTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        totalTable.addCell(new Phrase("")); // Empty cell for spacing
        PdfPCell totalCell = new PdfPCell(new Phrase("Total: " + currencyFormatter.format(totalAmount), boldBodyFont));
        totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalCell.setBorder(Rectangle.NO_BORDER);
        totalTable.addCell(totalCell);
        document.add(totalTable);

        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        // Signature section
        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(80);
        signatureTable.setHorizontalAlignment(Element.ALIGN_CENTER);
        signatureTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        signatureTable.addCell(new Phrase("Storekeeper", boldBodyFont));
        signatureTable.addCell(new Phrase("Requester", boldBodyFont));
        
        document.add(signatureTable);

        document.close();
        return baos.toByteArray();
    }
}
