package com.bytebites.notification.listener;

import com.bytebites.notification.config.RabbitMQConfig;
import com.bytebites.notification.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventListener.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        LOGGER.info("Received order placed event for customer {}: Order ID {}. Sending confirmation email...",
                event.customerId(), event.orderId());

        // Here, you would add logic to send an actual email or push notification.
        // For example, using JavaMailSender or a third-party notification service.
    }
}
