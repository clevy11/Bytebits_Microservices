package com.bytebites.order.service;

import com.bytebites.order.dto.OrderRequest;
import com.bytebites.order.model.Order;
import com.bytebites.order.model.OrderItem;
import com.bytebites.order.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.bytebites.order.config.RabbitMQConfig;
import com.bytebites.order.event.OrderPlacedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public OrderService(OrderRepository orderRepository, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    @Retry(name = "orderService")
    public Order createOrder(OrderRequest request, Long customerId) {
        Order order = new Order();

        List<OrderItem> orderItems = request.getItems().stream()
                .map(itemRequest -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setName(itemRequest.getName());
                    orderItem.setQuantity(itemRequest.getQuantity());
                    orderItem.setPrice(itemRequest.getPrice());
                    orderItem.setTotalPrice(itemRequest.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
                    orderItem.setOrder(order);
                    return orderItem;
                }).toList();

        BigDecimal totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setCustomerId(customerId);
        order.setRestaurantId(request.getRestaurantId());
        order.setTotalAmount(totalAmount);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

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

    private void publishOrderPlacedEvent(Order order) {
        List<OrderPlacedEvent.OrderItemData> itemData = order.getOrderItems().stream()
                .map(item -> new OrderPlacedEvent.OrderItemData(
                        item.getName(),
                        item.getQuantity(),
                        item.getPrice()
                )).toList();

        OrderPlacedEvent event = new OrderPlacedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getRestaurantId(),
                itemData,
                order.getTotalAmount()
        );

        // Add explicit type information to the message
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME, 
            RabbitMQConfig.ORDER_PLACED_ROUTING_KEY, 
            event,
            message -> {
                // Use a simple type ID that matches the mapping in the notification service
                message.getMessageProperties().setHeader("__TypeId__", "order.placed");
                return message;
            }
        );
        
        LOGGER.info("Published OrderPlacedEvent for order ID: {}, customer ID: {}", 
            order.getId(), order.getCustomerId());
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


} 