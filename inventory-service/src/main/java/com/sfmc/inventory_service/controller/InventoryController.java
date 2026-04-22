package com.sfmc.inventory_service.controller;

import com.sfmc.inventory_service.dto.StockCheckResponse;
import com.sfmc.inventory_service.dto.StockRequest;
import com.sfmc.inventory_service.dto.StockUpdateRequest;
import com.sfmc.inventory_service.entity.Stock;
import com.sfmc.inventory_service.entity.StockMovement;
import com.sfmc.inventory_service.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }


    @GetMapping("/stocks")
    public ResponseEntity<List<Stock>> getAllStocks() {
        return ResponseEntity.ok(inventoryService.getAllStocks());
    }


    @GetMapping("/stocks/{id}")
    public ResponseEntity<Stock> getStockById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getStockById(id));
    }


    @GetMapping("/stocks/warehouse/{warehouseId}")
    public ResponseEntity<List<Stock>> getByWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(inventoryService.getStocksByWarehouse(warehouseId));
    }


    @GetMapping("/stocks/product/{productId}")
    public ResponseEntity<List<Stock>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getStocksByProduct(productId));
    }


    @PostMapping("/stocks")
    public ResponseEntity<Stock> createStock(@RequestBody StockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.createStock(request));
    }

  
    @PutMapping("/stocks/{id}/update")
    public ResponseEntity<Stock> updateStock(@PathVariable Long id,
                                             @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.updateStock(id, request));
    }

    
    @GetMapping("/check")
    public ResponseEntity<StockCheckResponse> checkAvailability(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Double quantity) {
        return ResponseEntity.ok(inventoryService.checkAvailability(productId, warehouseId, quantity));
    }

   
    @GetMapping("/movements/{stockId}")
    public ResponseEntity<List<StockMovement>> getMovements(@PathVariable Long stockId) {
        return ResponseEntity.ok(inventoryService.getMovements(stockId));
    }

    // Décrémentation de stock par productId — appelé par order-service après validation commande
    @PutMapping("/decrease")
    public ResponseEntity<Stock> decreaseStock(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Double quantity,
            @RequestParam(required = false, defaultValue = "Commande validée") String reason) {
        return ResponseEntity.ok(inventoryService.decreaseStockByProduct(productId, warehouseId, quantity, reason));
    }
}
