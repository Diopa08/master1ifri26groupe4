package com.sfmc.orderservice.dto;

import com.sfmc.orderservice.model.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.sfmc.orderservice.dto.ClientDTO;

import java.time.LocalDateTime;
import java.util.List;

public class OrderDTO {

    public static class CreateOrderRequest {
        @NotNull private Long clientId;
        @NotEmpty @Valid private List<OrderItemRequest> items;
        @NotBlank private String shippingAddress;
        private String notes;

        public CreateOrderRequest() {}
        public CreateOrderRequest(Long clientId, List<OrderItemRequest> items, String shippingAddress, String notes) {
            this.clientId = clientId;
            this.items = items;
            this.shippingAddress = shippingAddress;
            this.notes = notes;
        }

        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }
        public List<OrderItemRequest> getItems() { return items; }
        public void setItems(List<OrderItemRequest> items) { this.items = items; }
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class OrderItemRequest {
        @NotNull private Long productId;
        @NotBlank private String productName;
        @Min(1) private int quantity;
        @DecimalMin("0.0") private double unitPrice;

        public OrderItemRequest() {}
        public OrderItemRequest(Long productId, String productName, int quantity, double unitPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    }

    public static class UpdateStatusRequest {
        @NotNull private OrderStatus newStatus;
        private String reason;

        public UpdateStatusRequest() {}
        public UpdateStatusRequest(OrderStatus newStatus, String reason) {
            this.newStatus = newStatus;
            this.reason = reason;
        }
        public OrderStatus getNewStatus() { return newStatus; }
        public void setNewStatus(OrderStatus newStatus) { this.newStatus = newStatus; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class OrderResponse {
        private Long id;
        private String orderNumber;
        private Long clientId;
        private OrderStatus status;
        private List<OrderItemResponse> items;
        private double totalAmount;
        private String shippingAddress;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOrderNumber() { return orderNumber; }
        public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
        public Long getClientId() { return clientId; }
        public void setClientId(Long clientId) { this.clientId = clientId; }
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
        public List<OrderItemResponse> getItems() { return items; }
        public void setItems(List<OrderItemResponse> items) { this.items = items; }
        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        private ClientDTO client;
		public ClientDTO getClient() { return client; }
		public void setClient(ClientDTO client) { this.client = client; }

    }

    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private int quantity;
        private double unitPrice;
        private double subtotal;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
        public double getSubtotal() { return subtotal; }
        public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    }
}