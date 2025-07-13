package com.bytebites.order.config;

import com.bytebites.order.event.OrderPlacedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "bytebites-exchange";
    public static final String ORDER_PLACED_ROUTING_KEY = "order.placed";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultClassMapper classMapper = new DefaultClassMapper();
        
        // Map the outgoing message type to the correct class
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("order.placed", OrderPlacedEvent.class);
        
        classMapper.setIdClassMapping(idClassMapping);
        classMapper.setTrustedPackages("com.bytebites.order.event");
        
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setExchange(EXCHANGE_NAME);
        return rabbitTemplate;
    }
}
