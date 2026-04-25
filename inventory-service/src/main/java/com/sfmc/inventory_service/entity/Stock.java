package com.sfmc.inventory_service.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String productName;
    private int quantity;
    private int criticalThreshold;  // seuil critique CDC
    private String warehouseId;     // multi-entrepôts CDC

    public Long getId() { return id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String name) { this.productName = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getCriticalThreshold() { return criticalThreshold; }
    public void setCriticalThreshold(int t) { this.criticalThreshold = t; }

    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }

    // ✅ Vérifier si stock sous le seuil critique
    public boolean isCritical() {
        return this.quantity <= this.criticalThreshold;
    }
}