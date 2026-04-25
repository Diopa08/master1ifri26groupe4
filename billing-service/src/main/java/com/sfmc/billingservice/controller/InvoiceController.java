package com.sfmc.billingservice.controller;



import com.sfmc.billingservice.model.Invoice;
import com.sfmc.billingservice.service.InvoiceService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/billing")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Invoice>> getAll() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @GetMapping("/invoices/my")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<Invoice>> getMyInvoices(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(invoiceService.getByEmail(email));
    }

    @PutMapping("/invoices/{id}/pay")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Invoice> markAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.markAsPaid(id));
    }
}