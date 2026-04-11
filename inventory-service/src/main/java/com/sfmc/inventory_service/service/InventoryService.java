package com.sfmc.inventory_service.service;

import com.sfmc.inventory_service.dto.StockCheckResponse;
import com.sfmc.inventory_service.dto.StockRequest;
import com.sfmc.inventory_service.dto.StockUpdateRequest;
import com.sfmc.inventory_service.entity.MovementType;
import com.sfmc.inventory_service.entity.Stock;
import com.sfmc.inventory_service.entity.StockMovement;
import com.sfmc.inventory_service.repository.StockMovementRepository;
import com.sfmc.inventory_service.repository.StockRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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

    // ─── Lecture ───────────────────────────────────────────────────────────────

    public List<Stock> getAllStocks() {
        return stockRepository.findAll();
    }

    public Stock getStockById(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Stock introuvable avec l'id : " + id));
    }

    public List<Stock> getStocksByWarehouse(Long warehouseId) {
        return stockRepository.findByWarehouseId(warehouseId);
    }

    public List<Stock> getStocksByProduct(Long productId) {
        return stockRepository.findByProductId(productId);
    }

    // ─── Création d'une entrée de stock ────────────────────────────────────────

    public Stock createStock(StockRequest request) {
        stockRepository.findByProductIdAndWarehouseId(request.getProductId(), request.getWarehouseId())
                .ifPresent(s -> { throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Une entrée de stock existe déjà pour ce produit dans cet entrepôt."); });

        Stock stock = new Stock(
                request.getProductId(),
                request.getQuantity(),
                request.getWarehouseId(),
                request.getThreshold()
        );
        return stockRepository.save(stock);
    }

    // ─── Mise à jour du stock (IN / OUT) ───────────────────────────────────────

    /**
     * Met à jour la quantité en stock et enregistre le mouvement correspondant.
     * Déclenché après production terminée (IN) ou après vente/perte (OUT).
     * (cf. cahier des charges §2.2 — Cas 2 : Mise à jour du stock)
     */
    @Transactional
    public Stock updateStock(Long stockId, StockUpdateRequest request) {
        Stock stock = getStockById(stockId);

        if (request.getType() == MovementType.OUT) {
            if (stock.getQuantity() < request.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Quantité insuffisante en stock. Disponible : " + stock.getQuantity());
            }
            stock.setQuantity(stock.getQuantity() - request.getQuantity());
        } else {
            stock.setQuantity(stock.getQuantity() + request.getQuantity());
        }

        stockRepository.save(stock);

        // Enregistrement du mouvement dans l'historique
        StockMovement movement = new StockMovement(
                stockId,
                request.getType(),
                request.getQuantity(),
                LocalDateTime.now(),
                request.getReason()
        );
        movementRepository.save(movement);

        return stock;
    }

    // ─── Vérification de disponibilité ─────────────────────────────────────────

    /**
     * Vérifie si la quantité demandée est disponible pour un produit dans un entrepôt.
     * Endpoint consommé par l'Order Service lors de la création d'une commande.
     * (cf. cahier des charges §2.2 — Cas 1 : Création d'une commande)
     */
    public StockCheckResponse checkAvailability(Long productId, Long warehouseId, Double requestedQuantity) {
        Stock stock = stockRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Aucun stock trouvé pour ce produit dans cet entrepôt."));

        boolean available = stock.getQuantity() >= requestedQuantity;
        boolean thresholdAlert = stock.getQuantity() <= stock.getThreshold();

        String message;
        if (available && !thresholdAlert) {
            message = "Stock disponible.";
        } else if (available) {
            message = "Stock disponible mais en dessous du seuil critique. Réapprovisionnement recommandé.";
        } else {
            message = "Stock insuffisant. Déclenchement de la production nécessaire.";
        }

        return new StockCheckResponse(available, stock.getQuantity(), thresholdAlert, message);
    }

    // ─── Historique des mouvements ──────────────────────────────────────────────

    public List<StockMovement> getMovements(Long stockId) {
        // Vérifie que le stock existe
        getStockById(stockId);
        return movementRepository.findByStockIdOrderByDateDesc(stockId);
    }
}
