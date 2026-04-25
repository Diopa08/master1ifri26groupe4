package com.sfmc.inventory_service.service;



import com.sfmc.inventory_service.dto.StockUpdatedEvent;
import com.sfmc.inventory_service.entity.Stock;
import com.sfmc.inventory_service.entity.StockMovement;
import com.sfmc.inventory_service.repository.StockMovementRepository;
import com.sfmc.inventory_service.repository.StockRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InventoryService {

    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;

    public InventoryService(StockRepository stockRepository,
                             StockMovementRepository movementRepository) {
        this.stockRepository = stockRepository;
        this.movementRepository = movementRepository;
    }

    // ✅ Vérifier disponibilité — appelé par order-service via Feign
    public boolean isAvailable(Long productId, int quantity) {
        return stockRepository.findByProductId(productId)
            .map(stock -> stock.getQuantity() >= quantity)
            .orElse(false);
    }

    // ✅ Réserver du stock — appelé par order-service via Feign
    @Transactional
    public void reserve(Long productId, int quantity) {
        Stock stock = stockRepository.findByProductId(productId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Produit introuvable en stock : " + productId));

        if (stock.getQuantity() < quantity) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Stock insuffisant");
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        stockRepository.save(stock);

        // Enregistrer le mouvement
        saveMovement(stock, "OUT", quantity, "SALE");

        // Alerte seuil critique
        if (stock.isCritical()) {
            System.out.println("⚠️ ALERTE : Stock critique pour produit "
                + productId + " → " + stock.getQuantity() + " unités");
        }
    }

    // ✅ Mise à jour stock depuis RabbitMQ (production terminée)
    @Transactional
    public void updateStockFromEvent(StockUpdatedEvent event) {
        Stock stock = stockRepository.findByProductId(event.productId())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Produit introuvable : " + event.productId()));

        if ("IN".equals(event.type())) {
            stock.setQuantity(stock.getQuantity() + event.quantityChanged());
        } else {
            stock.setQuantity(stock.getQuantity() - event.quantityChanged());
        }

        stockRepository.save(stock);
        saveMovement(stock, event.type(), event.quantityChanged(), "PRODUCTION");

        System.out.println("Stock mis à jour pour produit "
            + event.productId() + " → " + stock.getQuantity() + " unités");
    }

    // Lister tous les stocks
    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    // Stocks critiques
    public List<Stock> getCriticalStocks() {
        return stockRepository.findAll().stream()
            .filter(Stock::isCritical)
            .toList();
    }

    // Historique mouvements d'un produit
    public List<StockMovement> getMovements(Long productId) {
        return movementRepository.findByProductIdOrderByDateDesc(productId);
    }

    // Ajouter du stock manuellement
    @Transactional
    public Stock addStock(Long productId, int quantity, String reason) {
        Stock stock = stockRepository.findByProductId(productId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Produit introuvable : " + productId));

        stock.setQuantity(stock.getQuantity() + quantity);
        saveMovement(stock, "OUT", quantity, "SALE");

        return stockRepository.save(stock);
    }

    // Méthode privée utilitaire
    private void saveMovement(Stock stock, String type,
            int quantity, String reason) {
StockMovement movement = new StockMovement();
movement.setProductId(stock.getProductId());
movement.setStock(stock);          // ✅ lier au stock
movement.setType(type);
movement.setQuantity(quantity);
movement.setReason(reason);
movementRepository.save(movement);
}
}