package com.bytebites.notification.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Represents the event received when a new order is placed.
 * This record must match the structure of the event published by the order-service.
 */
public record OrderPlacedEvent(
        Long orderId,
        Long customerId,
        Long restaurantId,
        List<OrderItemData> items,
        BigDecimal totalAmount
) implements Serializable {

    /**
     * A simplified representation of an item within the order.
     */
    public record OrderItemData(
            String name,
            Integer quantity,
            BigDecimal price
    ) implements Serializable {}
}
