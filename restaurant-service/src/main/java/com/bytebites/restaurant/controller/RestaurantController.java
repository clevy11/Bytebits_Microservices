package com.bytebites.restaurant.controller;

import com.bytebites.restaurant.dto.RestaurantRequest;
import com.bytebites.restaurant.model.Restaurant;
import com.bytebites.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/restaurants")
@Tag(name = "Restaurants", description = "Restaurant management APIs")
@SecurityRequirement(name = "bearerAuth")
public class RestaurantController {

    @Autowired
    private RestaurantService restaurantService;

    @GetMapping
    @Operation(summary = "Get all active restaurants")
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant by ID")
    public ResponseEntity<Restaurant> getRestaurantById(@PathVariable Long id) {
        Optional<Restaurant> restaurant = restaurantService.getRestaurantById(id);
        return restaurant.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_RESTAURANT_OWNER')")
    @Operation(summary = "Create a new restaurant (Restaurant Owner only)")
    public ResponseEntity<Restaurant> createRestaurant(
            @Valid @RequestBody RestaurantRequest request) {
        
        // Get the authenticated user from the security context
        String ownerId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        Restaurant restaurant = restaurantService.createRestaurant(request, ownerId);
        return ResponseEntity.ok(restaurant);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_RESTAURANT_OWNER')")
    @Operation(summary = "Update restaurant (Owner only)")
    public ResponseEntity<Restaurant> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {
        
        // Get the authenticated user from the security context
        String ownerId = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<Restaurant> updatedRestaurant = restaurantService.updateRestaurant(id, request, ownerId);
        return updatedRestaurant.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-restaurants")
    @PreAuthorize("hasAuthority('ROLE_RESTAURANT_OWNER')")
    @Operation(summary = "Get restaurants owned by the current user")
    public ResponseEntity<List<Restaurant>> getMyRestaurants() {
        // Get the authenticated user from the security context
        String ownerId = SecurityContextHolder.getContext().getAuthentication().getName();
        
        List<Restaurant> restaurants = restaurantService.getRestaurantsByOwner(ownerId);
        return ResponseEntity.ok(restaurants);
    }
} 
