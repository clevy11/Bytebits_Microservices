package com.bytebites.notification.config;

import com.bytebites.notification.event.OrderPlacedEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
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
@EnableRabbit
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "bytebites-exchange";
    public static final String QUEUE_NAME = "notification-service-queue";
    public static final String ROUTING_KEY = "order.placed";

    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true, false, false);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        
        // Map the type ID to our local OrderPlacedEvent class
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("order.placed", OrderPlacedEvent.class);
        
        // Map the full class name to our local class
        idClassMapping.put("com.bytebites.order.event.OrderPlacedEvent", OrderPlacedEvent.class);
        
        classMapper.setIdClassMapping(idClassMapping);
        classMapper.setTrustedPackages("*");
        
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, 
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
