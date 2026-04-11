package com.sfmc.billingservice.service;

import com.sfmc.billingservice.dto.InvoiceDTO.GenerateInvoiceRequest;
import com.sfmc.billingservice.dto.InvoiceDTO.InvoiceResponse;
import com.sfmc.billingservice.dto.InvoiceDTO.RecordPaymentRequest;
import com.sfmc.billingservice.exception.BillingException.InvoiceAlreadyExistsException;
import com.sfmc.billingservice.exception.BillingException.InvoiceNotFoundException;
import com.sfmc.billingservice.exception.BillingException.InvoiceNotModifiableException;
import com.sfmc.billingservice.exception.BillingException.InvalidPaymentException;
import com.sfmc.billingservice.model.Invoice;
import com.sfmc.billingservice.model.InvoiceStatus;
import com.sfmc.billingservice.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final InvoiceRepository invoiceRepository;

    public BillingService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    // ── 1. Générer une facture ────────────────────────────────────────────────
    public InvoiceResponse generateInvoice(GenerateInvoiceRequest request) {
        log.info("Génération facture pour commande: {}", request.getOrderNumber());

        if (invoiceRepository.existsByOrderId(request.getOrderId())) {
            throw new InvoiceAlreadyExistsException(request.getOrderId());
        }

        String invoiceNumber = generateInvoiceNumber();

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setOrderId(request.getOrderId());
        invoice.setOrderNumber(request.getOrderNumber());
        invoice.setClientId(request.getClientId());
        invoice.setTotalAmount(request.getTotalAmount());
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setNotes(request.getNotes());

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Facture {} générée avec succès (montant: {} FCFA)", invoiceNumber, saved.getTotalAmount());

        return toResponse(saved);
    }

    // ── 2. Enregistrer un paiement ───────────────────────────────────────────
    public InvoiceResponse recordPayment(Long invoiceId, RecordPaymentRequest request) {
        log.info("Enregistrement paiement pour facture ID: {}", invoiceId);

        Invoice invoice = getInvoiceEntityById(invoiceId);

        if (invoice.getStatus() == InvoiceStatus.PAID
                || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new InvoiceNotModifiableException(invoice.getStatus().name());
        }

        if (request.getAmountPaid() > invoice.getTotalAmount()) {
            throw new InvalidPaymentException(
                "Le montant payé (" + request.getAmountPaid()
                + " FCFA) dépasse le total de la facture (" + invoice.getTotalAmount() + " FCFA)");
        }

        if (request.getAmountPaid().equals(invoice.getTotalAmount())) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDate.now());
            log.info("Facture {} entièrement payée", invoice.getInvoiceNumber());
        } else {
            invoice.setStatus(InvoiceStatus.PARTIAL);
            log.info("Facture {} partiellement payée: {} / {} FCFA",
                invoice.getInvoiceNumber(), request.getAmountPaid(), invoice.getTotalAmount());
        }

        invoice.setPaymentMethod(request.getPaymentMethod());
        if (request.getNotes() != null) {
            invoice.setNotes(request.getNotes());
        }

        Invoice saved = invoiceRepository.save(invoice);
        return toResponse(saved);
    }

    // ── 3. Récupérer une facture par ID ──────────────────────────────────────
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long invoiceId) {
        return toResponse(getInvoiceEntityById(invoiceId));
    }

    // ── 4. Récupérer la facture d'une commande ───────────────────────────────
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByOrderId(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
            .orElseThrow(() -> new InvoiceNotFoundException(
                "Aucune facture pour la commande ID : " + orderId));
        return toResponse(invoice);
    }

    // ── 5. Récupérer les factures d'un client ────────────────────────────────
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByClient(Long clientId) {
        return invoiceRepository.findByClientId(clientId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── 6. Récupérer toutes les factures ─────────────────────────────────────
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── 7. Récupérer les factures par statut ─────────────────────────────────
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── 8. Annuler une facture ───────────────────────────────────────────────
    public InvoiceResponse cancelInvoice(Long invoiceId) {
        Invoice invoice = getInvoiceEntityById(invoiceId);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvoiceNotModifiableException("PAID");
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        log.info("Facture {} annulée", invoice.getInvoiceNumber());
        return toResponse(invoiceRepository.save(invoice));
    }

    // ── 9. Tâche planifiée : marquer les factures en retard ──────────────────
    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdueInvoices() {
        log.info("Vérification des factures en retard...");

        List<Invoice> overdueInvoices = invoiceRepository
            .findByDueDateBeforeAndStatusNot(LocalDate.now(), InvoiceStatus.PAID);

        for (Invoice inv : overdueInvoices) {
            if (inv.getStatus() != InvoiceStatus.CANCELLED
                    && inv.getStatus() != InvoiceStatus.OVERDUE) {
                inv.setStatus(InvoiceStatus.OVERDUE);
                invoiceRepository.save(inv);
                log.warn("Facture {} marquée en retard (échéance: {})",
                    inv.getInvoiceNumber(), inv.getDueDate());
            }
        }

        log.info("{} facture(s) marquée(s) en retard", overdueInvoices.size());
    }

    // ── Méthodes privées ─────────────────────────────────────────────────────

    private Invoice getInvoiceEntityById(Long id) {
        return invoiceRepository.findById(id)
            .orElseThrow(() -> new InvoiceNotFoundException(id));
    }

    private String generateInvoiceNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = invoiceRepository.count() + 1;
        return String.format("FACT-%s-%04d", date, count);
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        InvoiceResponse response = new InvoiceResponse();
        response.setId(invoice.getId());
        response.setInvoiceNumber(invoice.getInvoiceNumber());
        response.setOrderId(invoice.getOrderId());
        response.setOrderNumber(invoice.getOrderNumber());
        response.setClientId(invoice.getClientId());
        response.setTotalAmount(invoice.getTotalAmount());
        response.setTaxAmount(invoice.getTaxAmount());
        response.setNetAmount(invoice.getNetAmount());
        response.setStatus(invoice.getStatus());
        response.setPaymentMethod(invoice.getPaymentMethod());
        response.setDueDate(invoice.getDueDate());
        response.setPaidAt(invoice.getPaidAt());
        response.setNotes(invoice.getNotes());
        response.setCreatedAt(invoice.getCreatedAt());
        response.setUpdatedAt(invoice.getUpdatedAt());
        return response;
    }
}
