package com.clinicboard.notification_service.application.usecase;

import com.clinicboard.notification_service.application.port.in.ProcessAppointmentEventUseCase;
import com.clinicboard.notification_service.application.port.out.NotificationGateway;
import com.clinicboard.notification_service.domain.event.AppointmentReminderEvent;
import com.clinicboard.notification_service.domain.event.AppointmentScheduledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementação do caso de uso para processamento de eventos de agendamento.
 * Coordena as operações mas delega responsabilidades específicas para portas de
 * saída.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessAppointmentEventUseCaseImpl implements ProcessAppointmentEventUseCase {

    private final NotificationGateway notificationGateway;

    @Override
    public void processAppointmentScheduledEvent(AppointmentScheduledEvent event) {
        log.info("Processing appointment scheduled event for appointment: {}", event.getAggregateId());

        try {

            notificationGateway.sendAppointmentScheduledNotification(
                    event.getAggregateId(),
                    event.getProfessionalId(),
                    event.getPatientId(),
                    event.getMessage());

            log.info("Successfully processed appointment scheduled event for appointment: {}", event.getAggregateId());

        } catch (Exception e) {
            log.error("Failed to process appointment scheduled event for appointment: {}", event.getAggregateId(), e);
            throw new RuntimeException("Failed to process appointment scheduled event", e);
        }
    }

    @Override
    public void processAppointmentReminderEvent(AppointmentReminderEvent event) {
        log.info("Processing appointment reminder event for appointment: {}", event.getAggregateId());

        try {
            notificationGateway.sendAppointmentReminderNotification(
                    event.getAppointmentId(),
                    event.getProfessionalId(),
                    event.getPatientId(),
                    event.getMessage());

            log.info("Successfully processed appointment reminder event for appointment: {}", event.getAggregateId());

        } catch (Exception e) {
            log.error("Failed to process appointment reminder event for appointment: {}", event.getAggregateId(), e);
            throw new RuntimeException("Failed to process appointment reminder event", e);
        }
    }

}
