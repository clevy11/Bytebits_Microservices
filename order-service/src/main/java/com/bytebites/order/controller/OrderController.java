package com.bytebites.order.controller;

import com.bytebites.order.dto.OrderRequest;
import com.bytebites.order.model.Order;
import com.bytebites.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(summary = "Place a new order (Customer only)")
    public ResponseEntity<Order> placeOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {
        // Get user ID from JWT token
        String userId = authentication.getName();
        Long customerId = Long.parseLong(userId);

        Order order = orderService.createOrder(request, customerId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(summary = "Get customer orders")
    public ResponseEntity<List<Order>> getMyOrders(Authentication authentication) {
        String userId = authentication.getName();
        Long customerId = Long.parseLong(userId);
        List<Order> orders = orderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    @Operation(summary = "Get specific order (Customer only)")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        
        String userId = authentication.getName();
        Long customerId = Long.parseLong(userId);
        Optional<Order> order = orderService.getOrderById(id, customerId);

        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    @Operation(summary = "Get restaurant orders (Restaurant Owner only)")
    public ResponseEntity<List<Order>> getRestaurantOrders(@PathVariable Long restaurantId) {
        List<Order> orders = orderService.getRestaurantOrders(restaurantId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ROLE_RESTAURANT_OWNER')")
    @Operation(summary = "Update order status (Restaurant Owner only)")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status,
            @RequestParam Long restaurantId) {

        Optional<Order> updatedOrder = orderService.updateOrderStatus(id, status, restaurantId);

        return updatedOrder.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 
