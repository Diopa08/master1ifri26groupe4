package com.sfmc.billingservice.repository;

import com.sfmc.billingservice.model.Invoice;
import com.sfmc.billingservice.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByOrderId(Long orderId);

    List<Invoice> findByClientId(Long clientId);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByClientIdAndStatus(Long clientId, InvoiceStatus status);

    List<Invoice> findByDueDateBeforeAndStatusNot(LocalDate date, InvoiceStatus status);

    boolean existsByOrderId(Long orderId);
}
