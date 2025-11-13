package com.clinicboard.business_service.application.port.out;

import com.clinicboard.business_service.application.dto.AppointmentReminderEvent;
import com.clinicboard.business_service.domain.event.AppointmentScheduledEvent;

/**
 * Gateway para publicação de eventos de negócio.
 * Esta interface define o contrato para publicação de eventos de domínio
 * que notificam outras partes do sistema sobre mudanças importantes
 * no contexto de agendamentos.
 */
public interface EventPublisherGateway {

    /**
     * Publica um evento de agendamento criado.
     * 
     * @param event o evento de agendamento criado
     */
    void publishAppointmentScheduled(AppointmentScheduledEvent event);

    void publishAppointmentReminderNotification(AppointmentReminderEvent event);

}
