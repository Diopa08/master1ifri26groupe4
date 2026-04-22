package com.sfmc.inventory_service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${notification.service.url:http://notification-service:8088}")
    private String notificationServiceUrl;

    public void sendStockAlert(Long productId, Long warehouseId, Double currentQty, Double threshold) {
        try {
            String message = String.format(
                "Stock bas détecté — Produit ID %d, Entrepôt ID %d. Quantité actuelle : %.0f (seuil : %.0f). Réapprovisionnement nécessaire.",
                productId, warehouseId, currentQty, threshold
            );
            Map<String, Object> payload = Map.of(
                "type", "STOCK_LOW",
                "title", "Alerte stock bas",
                "message", message,
                "targetRole", "ROLE_OPERATOR",
                "referenceId", productId,
                "referenceType", "STOCK"
            );
            restTemplate.postForEntity(notificationServiceUrl + "/api/notifications", payload, Void.class);
            log.info("Alerte stock bas envoyée pour produit ID {}", productId);
        } catch (Exception e) {
            log.warn("Impossible d'envoyer l'alerte stock : {}", e.getMessage());
        }
    }
}
