package com.bytebites.order.service;

import com.bytebites.order.dto.OrderRequest;
import com.bytebites.order.model.Order;
import com.bytebites.order.model.OrderItem;
import com.bytebites.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    @Retry(name = "orderService")
    public Order createOrder(OrderRequest request, Long customerId) {
        // Calculate total amount
        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> BigDecimal.valueOf(item.getQuantity()).multiply(BigDecimal.valueOf(10))) // Mock price
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setRestaurantId(request.getRestaurantId());
        order.setTotalAmount(totalAmount);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setSpecialInstructions(request.getSpecialInstructions());

        // Create order items
        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemRequest -> {
                    OrderItem item = new OrderItem();
                    item.setMenuItemId(itemRequest.getMenuItemId());
                    item.setMenuItemName("Menu Item " + itemRequest.getMenuItemId()); // Mock name
                    item.setQuantity(itemRequest.getQuantity());
                    item.setUnitPrice(BigDecimal.valueOf(10)); // Mock price
                    item.setTotalPrice(BigDecimal.valueOf(itemRequest.getQuantity()).multiply(BigDecimal.valueOf(10)));
                    item.setOrder(order);
                    return item;
                })
                .toList();

        order.setOrderItems(orderItems);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Publish order placed event
        publishOrderPlacedEvent(savedOrder);

        return savedOrder;
    }

    public Order createOrderFallback(OrderRequest request, Long customerId, Exception ex) {
        System.out.println("Circuit breaker triggered: " + ex.getMessage());
        
        // Create a fallback order with minimal information
        Order fallbackOrder = new Order();
        fallbackOrder.setCustomerId(customerId);
        fallbackOrder.setRestaurantId(request.getRestaurantId());
        fallbackOrder.setTotalAmount(BigDecimal.ZERO);
        fallbackOrder.setStatus(Order.OrderStatus.PENDING);
        fallbackOrder.setDeliveryAddress("Fallback address");
        
        return fallbackOrder;
    }

    public List<Order> getCustomerOrders(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getRestaurantOrders(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    public Optional<Order> getOrderById(Long orderId, Long customerId) {
        return orderRepository.findByIdAndCustomerId(orderId, customerId);
    }

    public Optional<Order> getOrderByIdForRestaurant(Long orderId, Long restaurantId) {
        return orderRepository.findByIdAndRestaurantId(orderId, restaurantId);
    }

    public Optional<Order> updateOrderStatus(Long orderId, Order.OrderStatus status, Long restaurantId) {
        Optional<Order> orderOpt = orderRepository.findByIdAndRestaurantId(orderId, restaurantId);
        
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(status);
            return Optional.of(orderRepository.save(order));
        }
        
        return Optional.empty();
    }

    private void publishOrderPlacedEvent(Order order) {
        String orderMessage = String.format(
            "Order placed: ID=%d, Customer=%d, Restaurant=%d, Amount=%s",
            order.getId(), order.getCustomerId(), order.getRestaurantId(), order.getTotalAmount()
        );
        
        rabbitTemplate.convertAndSend("order.placed", orderMessage);
        System.out.println("Published order placed event: " + orderMessage);
    }
} 