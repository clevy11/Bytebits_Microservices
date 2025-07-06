package com.bytebites.order.controller;

import com.bytebites.order.dto.OrderRequest;
import com.bytebites.order.model.Order;
import com.bytebites.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Place a new order (Customer only)")
    public ResponseEntity<Order> placeOrder(
            @Valid @RequestBody OrderRequest request,
            HttpServletRequest httpRequest) {
        
        Long customerId = Long.parseLong(httpRequest.getHeader("X-User-ID"));
        Order order = orderService.createOrder(request, customerId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get customer orders")
    public ResponseEntity<List<Order>> getMyOrders(HttpServletRequest httpRequest) {
        Long customerId = Long.parseLong(httpRequest.getHeader("X-User-ID"));
        List<Order> orders = orderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get specific order (Customer only)")
    public ResponseEntity<Order> getOrderById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        Long customerId = Long.parseLong(httpRequest.getHeader("X-User-ID"));
        Optional<Order> order = orderService.getOrderById(id, customerId);
        
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    @Operation(summary = "Get restaurant orders (Restaurant Owner only)")
    public ResponseEntity<List<Order>> getRestaurantOrders(@PathVariable Long restaurantId) {
        List<Order> orders = orderService.getRestaurantOrders(restaurantId);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
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