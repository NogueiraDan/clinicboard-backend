package com.clinicboard.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração do RabbitMQ para o serviço de notificação.
 * 
 * Esta configuração define:
 * - Exchanges para eventos e DLQ
 * - Filas específicas para cada tipo de evento
 * - Bindings entre exchanges e filas
 * - Conversor JSON para deserialização dos eventos
 * 
 * Alinhada com as configurações do business-service para garantir
 * compatibilidade na comunicação entre os serviços.
 */
@Configuration
public class RabbitMQConfig {

    // Exchanges
    @Value("${app.messaging.exchange.events}")
    private String eventsExchange;
    @Value("${app.messaging.dlq.exchange.events-failed}")
    private String dlqExchange;

    // Filas
    @Value("${app.messaging.queue.appointment-scheduled}")
    private String appointmentScheduledQueue;
    @Value("${app.messaging.queue.appointment-reminder}")
    private String appointmentReminderQueue;
    @Value("${app.messaging.dlq.queue.events-failed}")
    private String dlqQueue;

    // Routing Keys
    @Value("${app.messaging.routing-key.appointment-scheduled}")
    private String appointmentScheduledRoutingKey;
    @Value("${app.messaging.routing-key.appointment-reminder}")
    private String appointmentReminderRoutingKey;
    @Value("${app.messaging.dlq.routing-key.events-failed}")
    private String dlqRoutingKey;

    // ========== EXCHANGES ==========

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(eventsExchange, true, false);
    }

    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(dlqExchange, true, false);
    }

    // ========== FILAS ==========

    @Bean
    public Queue appointmentScheduledQueue() {
        return QueueBuilder.durable(appointmentScheduledQueue)
                .withArgument("x-dead-letter-exchange", dlqExchange)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    @Bean
    public Queue appointmentReminderQueue() {
        return QueueBuilder.durable(appointmentReminderQueue)
                .withArgument("x-dead-letter-exchange", dlqExchange)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(dlqQueue).build();
    }

    // ========== BINDINGS ==========

    @Bean
    public Binding appointmentScheduledBinding() {
        return BindingBuilder
                .bind(appointmentScheduledQueue())
                .to(eventsExchange())
                .with(appointmentScheduledRoutingKey);
    }

    @Bean
    public Binding appointmentReminderBinding() {
        return BindingBuilder
                .bind(appointmentReminderQueue())
                .to(eventsExchange())
                .with(appointmentReminderRoutingKey);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(dlqQueue())
                .to(dlqExchange())
                .with(dlqRoutingKey);
    }

    // ========== CONVERSOR JSON ==========

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();

        // Mapeamento dos eventos do business-service para os eventos locais
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.clinicboard.business_service.domain.event.AppointmentScheduledEvent",
                com.clinicboard.notification_service.notification.domain.event.AppointmentScheduledEvent.class);

        classMapper.setIdClassMapping(idClassMapping);
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
