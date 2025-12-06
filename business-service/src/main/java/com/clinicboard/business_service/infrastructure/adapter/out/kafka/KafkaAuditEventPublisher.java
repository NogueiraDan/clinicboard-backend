package com.clinicboard.business_service.infrastructure.adapter.out.kafka;

import com.clinicboard.business_service.domain.event.AppointmentAuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador de saída para publicação de eventos de auditoria no Kafka.
 * 
 * Responsável por enviar eventos imutáveis de mudanças em agendamentos
 * para o tópico de auditoria, permitindo rastreabilidade completa.
 * 
 * Segue o padrão Hexagonal Architecture como adaptador de infraestrutura.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAuditEventPublisher {

    private final KafkaTemplate<String, AppointmentAuditEvent> kafkaTemplate;
    
    @Value("${app.kafka.topic.appointment-audit}")
    private String auditTopic;

    /**
     * Publica evento de auditoria no Kafka.
     * 
     * Usa o aggregateId como chave para garantir ordenação por agendamento
     * na mesma partição (eventos do mesmo agendamento sempre na mesma ordem).
     * 
     * @param event Evento de auditoria a ser publicado
     */
    public void publishAuditEvent(AppointmentAuditEvent event) {
        log.info("📤 Publishing audit event to Kafka: {} - {} - {}", 
            event.getEventType(), 
            event.getAggregateId(),
            event.getEventId());
        
        kafkaTemplate.send(auditTopic, event.getAggregateId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("❌ Failed to publish audit event: {} - {}", 
                        event.getEventId(), 
                        ex.getMessage(), 
                        ex);
                } else {
                    log.info("✅ Audit event published successfully: {} to partition {}",
                        event.getEventId(), 
                        result.getRecordMetadata().partition());
                }
            });
    }
}
