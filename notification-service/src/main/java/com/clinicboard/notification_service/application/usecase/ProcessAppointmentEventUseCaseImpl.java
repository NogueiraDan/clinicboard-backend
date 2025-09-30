package com.clinicboard.notification_service.application.usecase;

import com.clinicboard.notification_service.application.port.in.ProcessAppointmentEventUseCase;
import com.clinicboard.notification_service.application.port.out.NotificationGateway;
import com.clinicboard.notification_service.domain.event.AppointmentScheduledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementação do caso de uso para processamento de eventos de agendamento.
 * Coordena as operações mas delega responsabilidades específicas para portas de saída.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessAppointmentEventUseCaseImpl implements ProcessAppointmentEventUseCase {

    private final NotificationGateway notificationGateway;

    @Override
    public void processAppointmentScheduled(AppointmentScheduledEvent event) {
        log.info("Processing appointment scheduled event for appointment: {}", event.getAggregateId());

        try {

            notificationGateway.sendAppointmentScheduledNotification(
                    event.getAggregateId(),
                    event.getUserId(),
                    event.getPatientId(),
                    event.getMessage());

            log.info("Successfully processed appointment scheduled event for appointment: {}", event.getAggregateId());

        } catch (Exception e) {
            log.error("Failed to process appointment scheduled event for appointment: {}", event.getAggregateId(), e);
            throw new RuntimeException("Failed to process appointment scheduled event", e);
        }
    }

}
