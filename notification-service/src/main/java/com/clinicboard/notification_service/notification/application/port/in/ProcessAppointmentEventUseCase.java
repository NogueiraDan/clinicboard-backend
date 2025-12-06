package com.clinicboard.notification_service.notification.application.port.in;

import com.clinicboard.notification_service.notification.domain.event.AppointmentReminderEvent;
import com.clinicboard.notification_service.notification.domain.event.AppointmentScheduledEvent;
/**
 * Porta de entrada para processamento de eventos de agendamento. * 
 * Segue o padrão Hexagonal Architecture onde as portas definem
 * os contratos de entrada da aplicação.
 */
public interface ProcessAppointmentEventUseCase {
    
    /**
     * Processa evento de agendamento criado
     */
    void processAppointmentScheduledEvent(AppointmentScheduledEvent event);
    void processAppointmentReminderEvent(AppointmentReminderEvent event);
}
