package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DemoEmailReminderCron {

    private static final Logger logger = LoggerFactory.getLogger(DemoEmailReminderCron.class);

    @Autowired
    private EmailService emailService;
    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private CustomerInvoiceRepository invoiceRepository;

    // Example: Send daily emails at 3:05 PM IST
    @Scheduled(cron = "0 45 00 * * ?", zone = "Asia/Kolkata")
    public void sendDailyInvoiceReminders() {
        logger.info("Starting daily invoice reminder cron job...");

        List<CustomerInvoice> invoices = invoiceRepository.findAll();
        logger.info("Fetched {} invoices from the database.", invoices.size());

        for (CustomerInvoice invoice : invoices) {
            if (!invoice.getIsPaid()) {  // Only unpaid invoices
                logger.info("Processing invoice ID: {}, Customer: {}", invoice.getId(), invoice.getCustomerName());

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
                    ByteArrayInputStream pdfStream = pdfGeneratorService.generatePdf(invoice);
                    byte[] pdfBytes = pdfStream.readAllBytes();

                    // Send email with PDF attachment
                    emailService.sendEmailWithAttachment(
                            invoice.getEmail(),
                            subject,
                            body,
                            pdfBytes,
                            "Invoice_" + invoice.getId() + ".pdf"
                    );
                    logger.info("Email sent successfully for invoice ID: {}", invoice.getId());

                     invoice.setMailsSent(invoice.getMailsSent() + 1);
                    invoiceRepository.save(invoice); 
                    Thread.sleep(3000); // 3 seconds
                } catch (Exception e) {
                    logger.error("Failed to send email for invoice ID: {}. Error: {}", invoice.getId(), e.getMessage(), e);
                }
            } else {
                logger.info("Skipping invoice ID: {} as it is already paid.", invoice.getId());
            }
        }

        logger.info("Daily invoice reminder cron job completed.");
    }
}
