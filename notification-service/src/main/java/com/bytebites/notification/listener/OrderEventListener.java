package com.bytebites.notification.listener;

import com.bytebites.notification.config.RabbitMQConfig;
import com.bytebites.notification.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventListener.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME, containerFactory = "rabbitListenerContainerFactory")
    public void handleOrderPlacedEvent(@Payload OrderPlacedEvent event) {
        try {
            LOGGER.info("Received order placed event for customer {}: Order ID {}. Sending confirmation email...",
                    event.customerId(), event.orderId());
            
            // Log the received event details
            LOGGER.debug("Order details - Restaurant ID: {}, Total Amount: {}, Items: {}",
                    event.restaurantId(), event.totalAmount(), event.items().size());

            // Here, you would add logic to send an actual email or push notification.
            // For example, using JavaMailSender or a third-party notification service.
            
        } catch (Exception e) {
            LOG.error("Error processing order placed event: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger retry or dead-letter queue if configured
        }
    }
}
