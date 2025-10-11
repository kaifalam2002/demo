package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.List;
@Component
public class DemoEmailReminderCron {
    

    @Autowired
    private EmailService emailService;

    @Autowired
    private CustomerInvoiceRepository invoiceRepository;

    // Example: Send daily emails at 9 AM
   @Scheduled(cron = "0 05 13 * * ?") // Every day at 9:00 AM
    public void sendDailyInvoiceReminders() {
        List<CustomerInvoice> invoices = invoiceRepository.findAll();

        for (CustomerInvoice invoice : invoices) {
            if (!invoice.getIsPaid()) {  // Only unpaid invoices
                String subject = "Reminder: Pending Invoice #" + invoice.getId();
                String body = "<html>"
                    + "<body>"
                    + "<p>Dear " + invoice.getCustomerName() + ",</p>"
                    + "<p>You have a pending invoice. Please find the details below:</p>"
                    + "<table border='1' cellpadding='8' cellspacing='0' style='border-collapse: collapse;'>"
                    + "<tr style='background-color:#f2f2f2;'>"
                    + "<th>Invoice ID</th>"
                    + "<th>Amount (₹)</th>"
                    + "<th>Due Date</th>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>" + invoice.getId() + "</td>"
                    + "<td>" + String.format("%.2f", invoice.getAmount()) + "</td>"
                    + "<td>" + invoice.getDueDate() + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "<p>Please find your invoice attached.</p>"
                    + "<p>Thank you!</p>"
                    + "</body>"
                    + "</html>";


                try {
                    // Generate PDF
                    ByteArrayInputStream pdfStream = PdfGeneratorService.generatePdf(invoice);
                    byte[] pdfBytes = pdfStream.readAllBytes();

                    // Send email with PDF attachment
                    emailService.sendEmailWithAttachment(
                            invoice.getEmail(),
                            subject,
                            body,
                            pdfBytes,
                            "Invoice_" + invoice.getId() + ".pdf"
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
