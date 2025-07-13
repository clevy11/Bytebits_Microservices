package com.bytebites.restaurant.repository;

import com.bytebites.restaurant.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    
    List<Restaurant> findByActiveTrue();
    
    List<Restaurant> findByOwnerId(String ownerId);
    
    Optional<Restaurant> findByIdAndOwnerId(Long id, String ownerId);
} 