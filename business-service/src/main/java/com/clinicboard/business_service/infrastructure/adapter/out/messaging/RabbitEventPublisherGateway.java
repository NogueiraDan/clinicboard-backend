package com.clinicboard.business_service.infrastructure.adapter.out.messaging;

import com.clinicboard.business_service.application.port.out.EventPublisherGateway;
import com.clinicboard.business_service.domain.event.AppointmentScheduledEvent;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;

/**
 * Implementação do gateway de eventos usando RabbitMQ com Circuit Breaker.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitEventPublisherGateway implements EventPublisherGateway {

    private final RabbitTemplate rabbitTemplate;
    private final DiscoveryClient discoveryClient;

    @Value("${app.messaging.exchange.events}")
    private String eventsExchange;

    @Value("${app.messaging.routing-key.appointment-scheduled}")
    private String appointmentScheduledRoutingKey;

    @Value("${app.messaging.dlq.exchange.events.dlq}")
    private String dlqExchange;

    @Value("${app.messaging.dlq.routing-key.events.failed}")
    private String dlqRoutingKey;

    @Value("${discovery.client.service.id}")
    private String serviceId;

    private boolean isNotificationServiceAvailable() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
            if (instances != null && !instances.isEmpty()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @CircuitBreaker(name = "business-service", fallbackMethod = "publishAppointmentScheduledFallback")
    @Retry(name = "business-service", fallbackMethod = "publishAppointmentScheduledFallback")
    public void publishAppointmentScheduled(AppointmentScheduledEvent event) {
        try {
            log.debug("Publishing appointment scheduled event for appointment: {}",
                    event.getAggregateId());

            if (isNotificationServiceAvailable()) {
                rabbitTemplate.convertAndSend(
                        eventsExchange,
                        appointmentScheduledRoutingKey,
                        event);
                log.info("Successfully published AppointmentScheduledEvent for appointment: {}",
                        event.getAggregateId());
            } else {
                throw new RuntimeException("Notification service not available");
            }

        } catch (Exception e) {
            log.error("Failed to publish AppointmentScheduledEvent for appointment: {}",
                    event.getAggregateId(), e);
            throw e; // Re-throw para acionar o Circuit Breaker
        }
    }

    /**
     * Fallback para AppointmentScheduledEvent - envia para DLQ
     */
    public void publishAppointmentScheduledFallback(AppointmentScheduledEvent event, Throwable throwable) {
        log.warn("Circuit breaker activated for AppointmentScheduledEvent. Sending to DLQ. Appointment: {}. Error: {}",
                event.getAggregateId(), throwable.getMessage());

        try {
            rabbitTemplate.convertAndSend(dlqExchange, dlqRoutingKey, event);
            log.info("AppointmentScheduledEvent sent to DLQ for appointment: {}. Reason: {}",
                    event.getAggregateId(), throwable.getMessage());
        } catch (Exception e) {
            log.error("Failed to send AppointmentScheduledEvent to DLQ for appointment: {}",
                    event.getAggregateId(), e);
        }
    }

}
