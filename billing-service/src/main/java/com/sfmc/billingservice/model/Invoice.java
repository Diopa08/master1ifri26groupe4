package com.sfmc.billingservice.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String invoiceNumber;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private Long clientId;

    @Column(nullable = false)
    private Double totalAmount;

    private Double taxAmount;

    private Double netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private LocalDate dueDate;

    private LocalDate paidAt;

    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = InvoiceStatus.UNPAID;
        }
        if (totalAmount != null) {
            this.taxAmount = totalAmount * 0.18;
            this.netAmount = totalAmount - this.taxAmount;
        }
        if (dueDate == null) {
            dueDate = LocalDate.now().plusDays(30);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Constructeurs ──────────────────────────────────────────────────────────

    public Invoice() {}

    public Invoice(Long id, String invoiceNumber, Long orderId, String orderNumber,
                   Long clientId, Double totalAmount, Double taxAmount, Double netAmount,
                   InvoiceStatus status, PaymentMethod paymentMethod,
                   LocalDate dueDate, LocalDate paidAt, String notes,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.clientId = clientId;
        this.totalAmount = totalAmount;
        this.taxAmount = taxAmount;
        this.netAmount = netAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.dueDate = dueDate;
        this.paidAt = paidAt;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public Long getOrderId() { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public Long getClientId() { return clientId; }
    public Double getTotalAmount() { return totalAmount; }
    public Double getTaxAmount() { return taxAmount; }
    public Double getNetAmount() { return netAmount; }
    public InvoiceStatus getStatus() { return status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getPaidAt() { return paidAt; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setId(Long id) { this.id = id; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public void setTaxAmount(Double taxAmount) { this.taxAmount = taxAmount; }
    public void setNetAmount(Double netAmount) { this.netAmount = netAmount; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setPaidAt(LocalDate paidAt) { this.paidAt = paidAt; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
