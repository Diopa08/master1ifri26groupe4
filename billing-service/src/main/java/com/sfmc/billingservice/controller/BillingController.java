package com.sfmc.billingservice.controller;

import com.sfmc.billingservice.dto.InvoiceDTO.GenerateInvoiceRequest;
import com.sfmc.billingservice.dto.InvoiceDTO.InvoiceResponse;
import com.sfmc.billingservice.dto.InvoiceDTO.RecordPaymentRequest;
import com.sfmc.billingservice.model.InvoiceStatus;
import com.sfmc.billingservice.service.BillingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
public class BillingController {

    private static final Logger log = LoggerFactory.getLogger(BillingController.class);

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    // POST /api/invoices/generate — générer une facture (appelé par Order Service)
    @PostMapping("/generate")
    public ResponseEntity<InvoiceResponse> generateInvoice(
            @Valid @RequestBody GenerateInvoiceRequest request) {
        log.info("POST /api/invoices/generate - commande: {}", request.getOrderNumber());
        InvoiceResponse response = billingService.generateInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/invoices — toutes les factures
    @GetMapping
    public ResponseEntity<List<InvoiceResponse>> getAllInvoices() {
        return ResponseEntity.ok(billingService.getAllInvoices());
    }

    // GET /api/invoices/{id} — une facture par ID
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(billingService.getInvoiceById(id));
    }

    // GET /api/invoices/order/{orderId} — facture d'une commande
    @GetMapping("/order/{orderId}")
    public ResponseEntity<InvoiceResponse> getInvoiceByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(billingService.getInvoiceByOrderId(orderId));
    }

    // GET /api/invoices/client/{clientId} — factures d'un client
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(billingService.getInvoicesByClient(clientId));
    }

    // GET /api/invoices/status/{status} — factures par statut
    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByStatus(
            @PathVariable InvoiceStatus status) {
        return ResponseEntity.ok(billingService.getInvoicesByStatus(status));
    }

    // POST /api/invoices/{id}/pay — enregistrer un paiement
    @PostMapping("/{id}/pay")
    public ResponseEntity<InvoiceResponse> recordPayment(
            @PathVariable Long id,
            @Valid @RequestBody RecordPaymentRequest request) {
        log.info("POST /api/invoices/{}/pay - montant: {} FCFA", id, request.getAmountPaid());
        return ResponseEntity.ok(billingService.recordPayment(id, request));
    }

    // DELETE /api/invoices/{id}/cancel — annuler une facture
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<InvoiceResponse> cancelInvoice(@PathVariable Long id) {
        log.info("DELETE /api/invoices/{}/cancel", id);
        return ResponseEntity.ok(billingService.cancelInvoice(id));
    }

    // GET /api/invoices/health — vérification du service
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> result = new HashMap<>();
        result.put("service", "billing-service");
        result.put("status", "UP");
        result.put("version", "1.0.0");
        return ResponseEntity.ok(result);
    }
}
