package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;

public interface CustomerInvoiceRepository extends JpaRepository<CustomerInvoice, Long> {

    // Custom method to find all invoices created by a specific owner
    ArrayList<CustomerInvoice> findByOwnerEmail(String ownerEmail);
}
