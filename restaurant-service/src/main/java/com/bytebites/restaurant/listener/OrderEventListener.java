package com.bytebites.restaurant.listener;

import com.bytebites.restaurant.config.RabbitMQConfig;
import com.bytebites.restaurant.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEventListener.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        LOGGER.info("Received order for restaurant {}: Order ID {}. Starting preparation...",
                event.restaurantId(), event.orderId());

        // Here, you would add logic to handle the order preparation,
        // such as updating internal dashboards or notifying kitchen staff.
    }
}
