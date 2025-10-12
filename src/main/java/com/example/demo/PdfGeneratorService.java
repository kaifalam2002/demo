package com.example.demo;

// ---------------- iText (PDF) ----------------
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

// ---------------- Apache POI (Excel) ----------------
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// ---------------- Spring / Java ----------------
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


@Service
public class PdfGeneratorService {

    @Autowired
private CustomerInvoiceRepository repository;


    public ByteArrayInputStream generatePdf(CustomerInvoice bill) {
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

    public void importFromExcel(MultipartFile file, String ownerEmail) {
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<CustomerInvoice> invoices = new ArrayList<>();

            // Skip header (start from row 1)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                CustomerInvoice invoice = new CustomerInvoice();
                invoice.setCustomerName(getStringCellValue(row.getCell(0)));
                invoice.setAddress(getStringCellValue(row.getCell(1)));
                invoice.setPhone(getStringCellValue(row.getCell(2)));
                invoice.setGstin(getStringCellValue(row.getCell(3)));
                invoice.setDueDate(getLocalDateCellValue(row.getCell(4)));
                invoice.setAmount(getBigDecimalCellValue(row.getCell(5)));
                invoice.setEmail(getStringCellValue(row.getCell(6)));
                invoice.setOwnerEmail(ownerEmail);
                invoice.setIsPaid(false);

                invoices.add(invoice);
            }

            repository.saveAll(invoices);
        } catch (Exception e) {
            throw new RuntimeException("Failed to import Excel file: " + e.getMessage(), e);
        }
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private BigDecimal getBigDecimalCellValue(Cell cell) {
        if (cell == null) return null;
        try {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } catch (Exception e) {
            try {
                return new BigDecimal(cell.getStringCellValue().trim());
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private LocalDate getLocalDateCellValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return LocalDate.parse(cell.getStringCellValue().trim());
            } catch (Exception ignored) {}
        }
        return null;
    }
}
