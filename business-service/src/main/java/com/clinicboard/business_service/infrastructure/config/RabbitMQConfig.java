package com.clinicboard.business_service.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ para eventos de agendamento (PRODUCER)
 * 
 * Como produtor, este serviço é responsável apenas por:
 * - Definir o Exchange principal
 * - Configurar o RabbitTemplate para publicação
 * - Definir routing keys para publicação
 * 
 * As filas específicas são responsabilidade dos consumidores.
 */
@Configuration
public class RabbitMQConfig {

    // Exchange Principal
    @Value("${app.messaging.exchange.events}")
    private String eventsExchange;

    // DLQ Configuration
    @Value("${app.messaging.dlq.exchange.events.dlq}")
    private String dlqExchange;

    @Value("${app.messaging.dlq.routing-key.events.failed}")
    private String dlqRoutingKey;

    // Routing Keys para publicação de eventos
    @Value("${app.messaging.routing-key.appointment-scheduled}")
    private String appointmentScheduledRoutingKey;

    @Value("${app.messaging.routing-key.appointment-reminder}")
    private String appointmentReminderRoutingKey;

    @Value("${app.messaging.routing-key.appointment-canceled}")
    private String appointmentCanceledRoutingKey;

    @Value("${app.messaging.routing-key.appointment-rescheduled}")
    private String appointmentRescheduledRoutingKey;

    /**
     * Exchange principal para publicação de eventos de agendamento
     * Topic Exchange permite routing baseado em patterns de routing keys
     */
    @Bean
    public TopicExchange eventsExchange() {
        return ExchangeBuilder
                .topicExchange(eventsExchange)
                .durable(true)
                .build();
    }

    /**
     * Exchange para Dead Letter Queue (DLQ)
     * Usado pelos fallbacks do Circuit Breaker quando o serviço de notificação está
     * indisponível
     */
    @Bean
    public TopicExchange dlqExchange() {
        return ExchangeBuilder
                .topicExchange(dlqExchange)
                .durable(true)
                .build();
    }

    /**
     * RabbitTemplate configurado para publicação de eventos
     * Usa conversor JSON para serialização automática dos eventos
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setMandatory(true); // Garante que mensagens sejam roteadas
        template.setExchange(eventsExchange); // Exchange padrão para publicação
        return template;
    }

    /**
     * Conversor JSON para serialização de eventos
     */
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
