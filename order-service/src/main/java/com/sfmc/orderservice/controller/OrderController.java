package com.sfmc.orderservice.controller;

import com.sfmc.orderservice.dto.ApiResponse;
import com.sfmc.orderservice.dto.OrderDTO.CreateOrderRequest;
import com.sfmc.orderservice.dto.OrderDTO.OrderResponse;
import com.sfmc.orderservice.dto.OrderDTO.UpdateStatusRequest;
import com.sfmc.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * POST /api/orders
     * Creer une nouvelle commande
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("POST /api/orders - client: {}", request.getClientId());
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(
                "Commande " + order.getOrderNumber() + " creee avec succes", order));
    }

    /**
     * GET /api/orders
     * Lister toutes les commandes
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(
            orders.size() + " commande(s) trouvee(s)", orders));
    }

    /**
     * GET /api/orders/{id}
     * Recuperer une commande par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(
            "Commande trouvee", order));
    }

    /**
     * GET /api/orders/client/{clientId}
     * Toutes les commandes d'un client
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByClient(
            @PathVariable Long clientId) {
        List<OrderResponse> orders = orderService.getOrdersByClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(
            orders.size() + " commande(s) pour le client " + clientId, orders));
    }

    /**
     * PUT /api/orders/{id}/validate
     * Valider une commande PENDING -> VALIDATED
     */
    @PutMapping("/{id}/validate")
    public ResponseEntity<ApiResponse<OrderResponse>> validateOrder(@PathVariable Long id) {
        log.info("PUT /api/orders/{}/validate", id);
        OrderResponse order = orderService.validateOrder(id);
        return ResponseEntity.ok(ApiResponse.success(
            "Commande " + order.getOrderNumber() + " validee avec succes", order));
    }

    /**
     * PUT /api/orders/{id}/status
     * Changer l'etat d'une commande
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        log.info("PUT /api/orders/{}/status -> {}", id, request.getNewStatus());
        OrderResponse order = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(
            "Statut mis a jour -> " + order.getStatus(), order));
    }

    /**
     * DELETE /api/orders/{id}/cancel
     * Annuler une commande
     */
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        OrderResponse order = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(ApiResponse.success(
            "Commande " + order.getOrderNumber() + " annulee", order));
    }
}