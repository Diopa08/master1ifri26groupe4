package com.sfmc.inventory_service.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "stocks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "warehouse_id"})
})
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Double quantity;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;


    @Column(nullable = false)
    private Double threshold;

    public Stock() {}

    public Stock(Long productId, Double quantity, Long warehouseId, Double threshold) {
        this.productId = productId;
        this.quantity = quantity;
        this.warehouseId = warehouseId;
        this.threshold = threshold;
    }

    public Long getId() { return id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }

    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }
}
