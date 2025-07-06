package com.bytebites.restaurant.service;

import com.bytebites.restaurant.dto.RestaurantRequest;
import com.bytebites.restaurant.model.Restaurant;
import com.bytebites.restaurant.repository.RestaurantRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findByActiveTrue();
    }

    public Optional<Restaurant> getRestaurantById(Long id) {
        return restaurantRepository.findById(id);
    }

    public Restaurant createRestaurant(RestaurantRequest request, Long ownerId) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setOwnerId(ownerId);
        
        return restaurantRepository.save(restaurant);
    }

    public Optional<Restaurant> updateRestaurant(Long id, RestaurantRequest request, Long ownerId) {
        Optional<Restaurant> existingRestaurant = restaurantRepository.findByIdAndOwnerId(id, ownerId);
        
        if (existingRestaurant.isPresent()) {
            Restaurant restaurant = existingRestaurant.get();
            restaurant.setName(request.getName());
            restaurant.setDescription(request.getDescription());
            restaurant.setAddress(request.getAddress());
            restaurant.setPhone(request.getPhone());
            
            return Optional.of(restaurantRepository.save(restaurant));
        }
        
        return Optional.empty();
    }

    public List<Restaurant> getRestaurantsByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId);
    }

    @RabbitListener(queues = "order.placed")
    public void handleOrderPlaced(String orderMessage) {
        // Process order placement event
        System.out.println("Restaurant service received order: " + orderMessage);
        // Here you would typically:
        // 1. Parse the order message
        // 2. Find the restaurant
        // 3. Update order status
        // 4. Send notification to kitchen staff
    }
} 