package com.bytebites.restaurant.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue orderPlacedQueue() {
        return QueueBuilder.durable("order.placed").build();
    }
}
