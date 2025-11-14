package com.clinicboard.notification_service.infrastructure.adapter.out;

import com.clinicboard.notification_service.application.port.out.NotificationGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implementação mock do gateway de notificações.
 * 
 * Esta implementação serve como placeholder inicial e pode ser
 * substituída por implementações reais (email, SMS, push notifications, etc).
 * 
 * Segue o padrão Hexagonal Architecture onde os adaptadores de saída
 * implementam as portas definidas pela camada de aplicação.
 */
@Slf4j
@Component
public class MockNotificationGateway implements NotificationGateway {
    
    @Override
    public void sendAppointmentScheduledNotification(String agregateId, String professionalId, String patientId, String message) {
        log.info("📅 [NOTIFICATION] Agendamento Criado");
        log.info("   Profissional: {} (ID: {})", professionalId, agregateId);
        log.info("   Paciente: {} (ID: {})", patientId, agregateId);
        log.info("   Mensagem: {}", message);
        log.info("   ===============================================");
        
        // TODO: Implementar envio real de notificação (email, SMS, push, etc)
        // Aqui seria integrado com serviços como SendGrid, Twilio, Firebase, etc.
    }

    @Override
    public void sendAppointmentReminderNotification(String appointmentId, String userId, String patientId,
            String message) {
        // TODO Auto-generated method stub
        log.info("⏰ [NOTIFICATION] Lembrete de Agendamento");
        log.info("   Usuário: {} (ID: {})", userId, appointmentId);
        log.info("   Paciente: {} (ID: {})", patientId, appointmentId);
        log.info("   Mensagem: {}", message);
        log.info("   ===============================================");
        
        // TODO: Implementar envio real de notificação (email, SMS, push, etc)
        // Aqui seria integrado com serviços como SendGrid, Twilio, Firebase, etc.
    }
}
