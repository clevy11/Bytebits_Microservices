package com.bytebites.order.repository;

import com.bytebites.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    List<Order> findByCustomerId(Long customerId);
    
    List<Order> findByRestaurantId(Long restaurantId);
    
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);
    
    Optional<Order> findByIdAndRestaurantId(Long id, Long restaurantId);
} 