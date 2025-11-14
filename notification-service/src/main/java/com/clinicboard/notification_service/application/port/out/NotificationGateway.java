package com.clinicboard.notification_service.application.port.out;

/**
 * Porta de saída para envio de notificações.
 * 
 * Define o contrato que os adaptadores de saída devem implementar
 * para enviar notificações através de diferentes canais (email, SMS, push,
 * etc).
 * 
 * Segue o padrão Hexagonal Architecture onde as portas definem
 * os contratos de saída da aplicação.
 */
public interface NotificationGateway {

    /**
     * Envia notificação de agendamento criado
     */
    void sendAppointmentScheduledNotification(String appointmentId, String userId, String patientId, String message);

    /**
     * Envia notificação de lembrete de agendamento
     */
    void sendAppointmentReminderNotification(String appointmentId, String userId, String patientId, String message);
}
