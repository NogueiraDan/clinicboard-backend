package com.clinicboard.notification_service.config;

import com.clinicboard.notification_service.audit.domain.event.AppointmentAuditEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração do consumidor Kafka para eventos de auditoria.
 * 
 * Responsabilidades:
 * - Configurar desserialização de eventos JSON
 * - Configurar grupos de consumidores
 * - Habilitar processamento concorrente
 * - Configurar tratamento de erros
 */
@Configuration
public class KafkaAuditConfig {
    
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    
    /**
     * Factory de consumidores Kafka com desserialização JSON.
     */
    @Bean
    public ConsumerFactory<String, AppointmentAuditEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Configurar desserializador JSON para confiar em todos os pacotes
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, AppointmentAuditEvent.class.getName());
        
        return new DefaultKafkaConsumerFactory<>(
            config,
            new StringDeserializer(),
            new JsonDeserializer<>(AppointmentAuditEvent.class)
        );
    }
    
    /**
     * Container factory para listeners Kafka.
     * 
     * Configurações:
     * - Concorrência: 3 threads para processar eventos em paralelo
     * - Auto startup: Inicia automaticamente quando aplicação sobe
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AppointmentAuditEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, AppointmentAuditEvent> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // 3 threads consumindo em paralelo
        
        return factory;
    }
}