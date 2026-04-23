package com.sfmc.orderservice.service;

import com.sfmc.orderservice.dto.OrderDTO.CreateOrderRequest;
import com.sfmc.orderservice.dto.OrderDTO.OrderItemResponse;
import com.sfmc.orderservice.dto.OrderDTO.OrderResponse;
import com.sfmc.orderservice.dto.OrderDTO.UpdateStatusRequest;
import com.sfmc.orderservice.event.BillingRequest;
import com.sfmc.orderservice.event.BillingServiceClient;
import com.sfmc.orderservice.exception.OrderException.InvalidStatusTransitionException;
import com.sfmc.orderservice.exception.OrderException.OrderNotFoundException;
import com.sfmc.orderservice.model.Order;
import com.sfmc.orderservice.model.OrderItem;
import com.sfmc.orderservice.model.OrderStatus;
import com.sfmc.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.sfmc.orderservice.event.AuthServiceClient;
import com.sfmc.orderservice.dto.ClientDTO;

@Service
@Transactional
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final BillingServiceClient billingServiceClient;
    private final AuthServiceClient authServiceClient;

    public OrderService(OrderRepository orderRepository,
            BillingServiceClient billingServiceClient,
            AuthServiceClient authServiceClient) {
	this.orderRepository = orderRepository;
	this.billingServiceClient = billingServiceClient;
	this.authServiceClient = authServiceClient;
	}

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
        OrderStatus.PENDING,       Set.of(OrderStatus.VALIDATED, OrderStatus.CANCELLED),
        OrderStatus.VALIDATED,     Set.of(OrderStatus.IN_PRODUCTION, OrderStatus.SHIPPED, OrderStatus.CANCELLED),
        OrderStatus.IN_PRODUCTION, Set.of(OrderStatus.VALIDATED, OrderStatus.CANCELLED),
        OrderStatus.SHIPPED,       Set.of(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED,     Set.of(),
        OrderStatus.CANCELLED,     Set.of()
    );

    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creation commande pour client ID: {}", request.getClientId());

        List<OrderItem> items = request.getItems().stream()
            .map(itemReq -> {
                OrderItem item = new OrderItem();
                item.setProductId(itemReq.getProductId());
                item.setProductName(itemReq.getProductName());
                item.setQuantity(itemReq.getQuantity());
                item.setUnitPrice(itemReq.getUnitPrice());
                return item;
            })
            .collect(Collectors.toList());

        double total = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
        String orderNumber = generateOrderNumber();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setClientId(request.getClientId());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(total);
        order.setShippingAddress(request.getShippingAddress());
        order.setNotes(request.getNotes());

        items.forEach(item -> item.setOrder(order));
        order.setItems(items);

        Order saved = orderRepository.save(order);
        log.info("Commande creee : {}", saved.getOrderNumber());
        return toResponse(saved);
    }

    public OrderResponse validateOrder(Long orderId) {
        log.info("Validation commande ID: {}", orderId);
        Order order = getOrderEntityById(orderId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                order.getStatus().name(), OrderStatus.VALIDATED.name());
        }

        order.setStatus(OrderStatus.VALIDATED);
        Order saved = orderRepository.save(order);

        try {
            BillingRequest billing = new BillingRequest();
            billing.setOrderId(saved.getId());
            billing.setOrderNumber(saved.getOrderNumber());
            billing.setClientId(saved.getClientId());
            billing.setTotalAmount(saved.getTotalAmount());
            billingServiceClient.generateInvoice(billing);
            log.info("Facture generee pour commande {}", saved.getOrderNumber());
        } catch (Exception e) {
            log.error("Erreur facturation: {}", e.getMessage());
        }

        return toResponse(saved);
    }

    public OrderResponse updateOrderStatus(Long orderId, UpdateStatusRequest request) {
        log.info("Changement etat commande ID: {} -> {}", orderId, request.getNewStatus());
        Order order = getOrderEntityById(orderId);
        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = request.getNewStatus();

        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidStatusTransitionException(currentStatus.name(), newStatus.name());
        }

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        log.info("Commande {} : {} -> {}", order.getOrderNumber(), currentStatus, newStatus);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {


        return toResponse(getOrderEntityById(orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByClient(Long clientId) {
        return orderRepository.findByClientId(clientId).stream()
            .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
    	log.info("### CONTROLLER getAllOrders APPELE ###");
    	        return orderRepository.findAll().stream()
            .map(this::toResponse).collect(Collectors.toList());
    }

    public OrderResponse cancelOrder(Long orderId, String reason) {


        UpdateStatusRequest req = new UpdateStatusRequest(OrderStatus.CANCELLED, reason);
        return updateOrderStatus(orderId, req);
    }

    private Order getOrderEntityById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderRepository.count() + 1;
        return String.format("CMD-%s-%04d", date, count);
    }

    private OrderResponse toResponse(Order order) {
    	System.out.println("TO RESPONSE APPELEE");
        List<OrderItemResponse> itemResponses = order.getItems() == null ? List.of() :
            order.getItems().stream().map(item -> {
                OrderItemResponse r = new OrderItemResponse();
                r.setId(item.getId());
                r.setProductId(item.getProductId());
                r.setProductName(item.getProductName());
                r.setQuantity(item.getQuantity());
                r.setUnitPrice(item.getUnitPrice());
                r.setSubtotal(item.getSubtotal());
                return r;
            }).collect(Collectors.toList());

        
        OrderResponse r = new OrderResponse();
        r.setId(order.getId());
        r.setOrderNumber(order.getOrderNumber());
        r.setClientId(order.getClientId());
        ClientDTO client = authServiceClient.getClientById(order.getClientId());
        log.info("CLIENT RECUPERE = {}", client);
        
        r.setClient(client);
        r.setStatus(order.getStatus());
        r.setItems(itemResponses);
        r.setTotalAmount(order.getTotalAmount());
        r.setShippingAddress(order.getShippingAddress());
        r.setNotes(order.getNotes());
        r.setCreatedAt(order.getCreatedAt());
        r.setUpdatedAt(order.getUpdatedAt());
        return r;
    }
}