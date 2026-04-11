package com.sfmc.inventory_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_id", nullable = false)
    private Long stockId;

    // IN (entrée) ou OUT (sortie)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private LocalDateTime date;

    private String reason;

    public StockMovement() {}

    public StockMovement(Long stockId, MovementType type, Double quantity, LocalDateTime date, String reason) {
        this.stockId = stockId;
        this.type = type;
        this.quantity = quantity;
        this.date = date;
        this.reason = reason;
    }

    public Long getId() { return id; }

    public Long getStockId() { return stockId; }
    public void setStockId(Long stockId) { this.stockId = stockId; }

    public MovementType getType() { return type; }
    public void setType(MovementType type) { this.type = type; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
