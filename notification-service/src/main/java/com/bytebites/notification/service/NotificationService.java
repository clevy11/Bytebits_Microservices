package com.bytebites.notification.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @RabbitListener(queues = "order.placed")
    public void handleOrderPlaced(String orderMessage) {
        System.out.println("=== NOTIFICATION SERVICE ===");
        System.out.println("📧 Sending email notification for order: " + orderMessage);
        System.out.println("📱 Sending push notification for order: " + orderMessage);
        System.out.println("✅ Notifications sent successfully!");
        System.out.println("=============================");
        
        // In a real implementation, you would:
        // 1. Parse the order message to extract customer and order details
        // 2. Send email notification to customer
        // 3. Send push notification to customer's mobile app
        // 4. Send notification to restaurant about new order
        // 5. Log notification events
    }
} 