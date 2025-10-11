package com.example.demo;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

@Service
public class PdfGeneratorService {

    public static ByteArrayInputStream generatePdf(CustomerInvoice bill) {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Add company logo (replace with your logo path or load from resources)
            try {
                Image logo = Image.getInstance("src/main/resources/static/logo.png"); // update path accordingly
                logo.scaleToFit(100, 50);
                logo.setAlignment(Element.ALIGN_CENTER);
                document.add(logo);
            } catch (Exception e) {
                // Logo not found, continue without crashing
                System.out.println("Logo not found: " + e.getMessage());
            }

            // Company name and address
            Font companyNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLUE);
            Paragraph companyName = new Paragraph("ACME Corporation", companyNameFont);
            companyName.setAlignment(Element.ALIGN_CENTER);
            document.add(companyName);

            Font companyAddressFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.DARK_GRAY);
            Paragraph companyAddress = new Paragraph("1234 Market Street, City, Country\nPhone: +1 234 567 8900\nEmail: info@acme.com", companyAddressFont);
            companyAddress.setAlignment(Element.ALIGN_CENTER);
            document.add(companyAddress);

            document.add(Chunk.NEWLINE);

            // Bill title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Customer Bill Receipt", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE);

            // Customer details table
            PdfPTable customerTable = new PdfPTable(2);
            customerTable.setWidthPercentage(100);
            customerTable.setSpacingBefore(10f);
            customerTable.setSpacingAfter(10f);

            // Column widths
            float[] columnWidths = {2f, 4f};
            customerTable.setWidths(columnWidths);

            // Table header style
            Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD);

            customerTable.addCell(new PdfPCell(new Phrase("Name", headFont)));
           // customerTable.addCell(new PdfPCell(new Phrase(bill.getName())));

            customerTable.addCell(new PdfPCell(new Phrase("Email", headFont)));
            customerTable.addCell(new PdfPCell(new Phrase(bill.getEmail())));

            customerTable.addCell(new PdfPCell(new Phrase("Phone", headFont)));
            customerTable.addCell(new PdfPCell(new Phrase(bill.getPhone())));

            customerTable.addCell(new PdfPCell(new Phrase("GST Number", headFont)));
           // customerTable.addCell(new PdfPCell(new Phrase(bill.getGst())));

            document.add(customerTable);

            // Amount details table
            PdfPTable amountTable = new PdfPTable(2);
            amountTable.setWidthPercentage(50);
            amountTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            amountTable.setSpacingBefore(10f);

            amountTable.setWidths(new float[]{3f, 2f});

            amountTable.addCell(new PdfPCell(new Phrase("Description", headFont)));
            amountTable.addCell(new PdfPCell(new Phrase("Amount", headFont)));

            amountTable.addCell(new PdfPCell(new Phrase("Service Charges")));
            amountTable.addCell(new PdfPCell(new Phrase(String.format("₹ %.2f", bill.getAmount()))));

            //double gstAmount = bill.getAmount() * 0.18; // assuming 18% GST
            amountTable.addCell(new PdfPCell(new Phrase("GST (18%)")));
           // amountTable.addCell(new PdfPCell(new Phrase(String.format("₹ %.2f", gstAmount))));

            PdfPCell totalLabelCell = new PdfPCell(new Phrase("Total Amount", headFont));
            totalLabelCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            amountTable.addCell(totalLabelCell);

            // PdfPCell totalAmountCell = new PdfPCell(new Phrase(String.format("₹ %.2f", bill.getAmount() + gstAmount), headFont));
            // totalAmountCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            // amountTable.addCell(totalAmountCell);

            document.add(amountTable);

            document.add(Chunk.NEWLINE);

            // Thank you note
            Font thanksFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12, BaseColor.DARK_GRAY);
            Paragraph thanks = new Paragraph("Thank you for your purchase!", thanksFont);
            thanks.setAlignment(Element.ALIGN_CENTER);
            document.add(thanks);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
