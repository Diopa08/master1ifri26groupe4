package com.sfmc.billingservice.service;



import com.sfmc.billingservice.dto.OrderCreatedEvent;
import com.sfmc.billingservice.model.Invoice;
import com.sfmc.billingservice.model.InvoiceStatus;
import com.sfmc.billingservice.repository.InvoiceRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InvoiceService {

    private static final Logger log =
        LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public Invoice createInvoiceFromOrder(OrderCreatedEvent event) {

        // Éviter les doublons
        if (invoiceRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.info("Facture déjà existante pour commande : {}",
                event.getOrderId());
            return invoiceRepository.findByOrderId(
                event.getOrderId()).get();
        }

        Invoice invoice = new Invoice();
        invoice.setOrderId(event.getOrderId());         // ✅ getOrderId()
        invoice.setClientId(event.getClientId());       // ✅ getClientId()
        invoice.setTotalAmount(event.getTotalAmount()); // ✅ getTotalAmount()
        invoice.setCustomerEmail(event.getEmail());     // ✅ getEmail()
        invoice.setStatus(InvoiceStatus.PENDING);

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Facture créée : #{} pour commande : {}",
            saved.getId(), event.getOrderId());

        return saved;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> getByEmail(String email) {
        return invoiceRepository.findByCustomerEmail(email);
    }

    @Transactional
    public Invoice markAsPaid(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Facture introuvable : " + id));

        invoice.setStatus(InvoiceStatus.PAID);
        return invoiceRepository.save(invoice);
    }
}