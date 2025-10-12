package com.example.demo;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.List;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
@CrossOrigin // if you're calling from frontend
public class CustomerBillController {

   @Autowired
private CustomerInvoiceRepository repo;
@Autowired
    private EmailService emailService;  

    @Autowired
    private UserRepository userRepository;

     @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @PostMapping("/download-bill")
public ResponseEntity<byte[]> downloadBill(@ModelAttribute CustomerInvoice bill, HttpSession session) {
    String ownerEmail = (String) session.getAttribute("loggedInEmail");
    bill.setOwnerEmail(ownerEmail);

    CustomerInvoice saved = repo.save(bill);

    ByteArrayInputStream pdfStream = pdfGeneratorService.generatePdf(saved);

    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=bill.pdf");

    return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfStream.readAllBytes());
}


@GetMapping("/track-bills")
public ResponseEntity<ArrayList<CustomerInvoice>> trackBills(HttpSession session) {
    String ownerEmail = (String) session.getAttribute("loggedInEmail");
    if (ownerEmail == null) {
        return ResponseEntity.status(401).build(); // Unauthorized if not logged in
    }

    ArrayList<CustomerInvoice> bills = repo.findByOwnerEmail(ownerEmail);
    return ResponseEntity.ok(bills);
}

// In CustomerBillController or CustomerInvoiceController
@PutMapping("/update-paid-status/{id}")
public ResponseEntity<Object> updatePaidStatus(@PathVariable Long id, HttpSession session) {
    String ownerEmail = (String) session.getAttribute("loggedInEmail");
    if (ownerEmail == null) {
        return ResponseEntity.status(401).build();
    }

    return repo.findById(id)
            .map(bill -> {
                // Only allow the owner to update
                if (!ownerEmail.equals(bill.getOwnerEmail())) {
                    return ResponseEntity.status(403).build(); // Forbidden
                }

                bill.setIsPaid(!bill.getIsPaid()); // toggle paid status
                repo.save(bill);
                return ResponseEntity.ok().build();
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
}

@PostMapping("/send-mail")
public ResponseEntity<String> sendMail(@RequestParam String to, @RequestParam String subject, @RequestParam String text) {
    emailService.sendSimpleEmail(to, subject, text);
    return ResponseEntity.ok("Email sent successfully!");
}

@GetMapping("/user-info")
    public ResponseEntity<User> getUserInfo(HttpSession session) {
        String email = (String) session.getAttribute("loggedInEmail");
        if (email == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(userOpt.get());
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file,
                                              @RequestParam("ownerEmail") String ownerEmail) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a valid Excel file.");
        }

        try {
            pdfGeneratorService.importFromExcel(file, ownerEmail);
            return ResponseEntity.ok("File uploaded and data imported successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
