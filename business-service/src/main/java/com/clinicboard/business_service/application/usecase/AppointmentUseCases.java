package com.clinicboard.business_service.application.usecase;

import java.time.LocalDate;
import java.util.List;

import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import com.clinicboard.business_service.application.dto.AppointmentRequestDto;
import com.clinicboard.business_service.application.dto.AppointmentResponseDto;
import com.clinicboard.business_service.application.port.in.AppointmentUseCasesPort;
import com.clinicboard.business_service.application.port.out.AppointmentPersistencePort;
import com.clinicboard.business_service.application.port.out.EventPublisherGateway;
import com.clinicboard.business_service.domain.event.AppointmentAuditEvent;
import com.clinicboard.business_service.domain.event.AppointmentScheduledEvent;
import com.clinicboard.business_service.domain.service.AppointmentSchedulingService;
import com.clinicboard.business_service.infrastructure.adapter.out.kafka.KafkaAuditEventPublisher;
import com.clinicboard.business_service.infrastructure.adapter.out.quartz.AppointmentReminderScheduler;

@Slf4j
@Service
public class AppointmentUseCases implements AppointmentUseCasesPort {

    private final AppointmentPersistencePort appointmentPersistencePort;
    private final EventPublisherGateway eventPublisher;
    private final AppointmentReminderScheduler appointmentReminderScheduler;
    private final AppointmentSchedulingService appointmentSchedulingService;
    private final KafkaAuditEventPublisher kafkaAuditEventPublisher;

    public AppointmentUseCases(AppointmentPersistencePort appointmentPersistencePort,
            EventPublisherGateway eventPublisher, AppointmentReminderScheduler appointmentReminderScheduler,
            AppointmentSchedulingService appointmentSchedulingService,
            KafkaAuditEventPublisher kafkaAuditEventPublisher) {
        this.appointmentPersistencePort = appointmentPersistencePort;
        this.eventPublisher = eventPublisher;
        this.appointmentReminderScheduler = appointmentReminderScheduler;
        this.appointmentSchedulingService = appointmentSchedulingService;
        this.kafkaAuditEventPublisher = kafkaAuditEventPublisher;
    }

    @Override
    public AppointmentResponseDto create(AppointmentRequestDto appointment) {
        appointmentSchedulingService.checkPatientHasNoSchedulingOnSameDate(appointment.getPatient_id(),
                appointment.getDate());
        appointmentSchedulingService.checkNoSchedulingOnSameDateTime(appointment.getDate(), appointment.getHour());
        AppointmentResponseDto appointmentResponse = appointmentPersistencePort.create(appointment);
        publishAuditEvent(appointmentResponse, "CREATED", appointment.getUser_id());
        publishAppointmentScheduledEvent(appointmentResponse);
        try {
            appointmentReminderScheduler.scheduleReminder(appointmentResponse);
        } catch (SchedulerException e) {
            throw new RuntimeException("Falha ao agendar lembrete Quartz: " + e.getMessage(), e);
        }
        return appointmentResponse;
    }

    @Override
    public List<AppointmentResponseDto> findAll() {
        return appointmentPersistencePort.findAll();
    }

    @Override
    public AppointmentResponseDto update(String id, AppointmentRequestDto appointment) {
        appointmentSchedulingService.checkNoSchedulingOnSameDateTime(appointment.getDate(), appointment.getHour());
        appointmentSchedulingService.validateTypeUpdateScheduling(appointment.getType());
        // TODO: Implementar o findById para buscar o agendamento antigo
        // // ✅ Buscar agendamento antigo para auditoria
        // AppointmentResponseDto oldAppointment = appointmentPersistencePort.findById(id)
        //     .orElseThrow(() -> new BusinessException("Agendamento não encontrado"));
        
        // AppointmentResponseDto updatedAppointment = appointmentPersistencePort.update(id, appointment);
        
        // // ✅ PUBLICAR EVENTO DE AUDITORIA NO KAFKA (REMARCAÇÃO)
        // publishRescheduleAuditEvent(
        //     oldAppointment, 
        //     updatedAppointment, 
        //     appointment.getUser_id());
        
        // return updatedAppointment;
        return appointmentPersistencePort.update(id, appointment);
    }

    @Override
    public void delete(String id) {
        appointmentPersistencePort.delete(id);
        // TODO: Implementar auditoria de exclusão
        // ✅ Buscar agendamento antes de deletar para auditoria
        // AppointmentResponseDto appointment = appointmentPersistencePort.findById(id)
        //     .orElseThrow(() -> new BusinessException("Agendamento não encontrado"));
        
        // appointmentPersistencePort.delete(id);
        
        // // ✅ PUBLICAR EVENTO DE AUDITORIA NO KAFKA (CANCELAMENTO)
        // publishCancellationAuditEvent(appointment, "SYSTEM", "Agendamento excluído pelo sistema");
    }

    @Override
    public List<AppointmentResponseDto> findAppointments(String userId, LocalDate date) {
        return appointmentPersistencePort.findAppointments(userId, date);
    }

    @Override
    public List<String> findAvailableHours(LocalDate date, String userId) {
        return appointmentPersistencePort.findAvailableHours(date, userId);
    }

    private void publishAppointmentScheduledEvent(AppointmentResponseDto appointment) {
        try {
            var event = AppointmentScheduledEvent.from(
                    appointment.getId(),
                    appointment.getPatient_id(),
                    appointment.getUser_id(),
                    appointment.getDate().toString(),
                    appointment.getHour());

            eventPublisher.publishAppointmentScheduled(event);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Falha ao publicar evento de agendamento: " + e.getMessage(), e);
        }
    }

    /**
     * Publica evento de auditoria para criação de agendamento.
     */
    private void publishAuditEvent(AppointmentResponseDto appointment, String eventType, String changedBy) {
        try {
            AppointmentAuditEvent auditEvent = AppointmentAuditEvent.created(
                    appointment.getId(),
                    appointment.getUser_id(),
                    appointment.getPatient_id(),
                    appointment.getDate().toString(),
                    appointment.getHour(),
                    changedBy);

            kafkaAuditEventPublisher.publishAuditEvent(auditEvent);
        } catch (Exception e) {
            log.error("Falha ao publicar evento de auditoria: {}", e.getMessage(), e);
            // NÃO LANÇA EXCEÇÃO - Auditoria não deve impedir operação principal
        }
    }

    /**
     * Publica evento de auditoria para remarcação de agendamento.
     */
    private void publishRescheduleAuditEvent(
            AppointmentResponseDto oldAppointment,
            AppointmentResponseDto newAppointment,
            String changedBy) {
        try {
            AppointmentAuditEvent auditEvent = AppointmentAuditEvent.rescheduled(
                    newAppointment.getId(),
                    newAppointment.getUser_id(),
                    newAppointment.getPatient_id(),
                    oldAppointment.getDate().toString(),
                    oldAppointment.getHour(),
                    newAppointment.getDate().toString(),
                    newAppointment.getHour(),
                    changedBy);

            kafkaAuditEventPublisher.publishAuditEvent(auditEvent);
        } catch (Exception e) {
            log.error("Falha ao publicar evento de auditoria: {}", e.getMessage(), e);
        }
    }

    /**
     * Publica evento de auditoria para cancelamento de agendamento.
     */
    private void publishCancellationAuditEvent(
            AppointmentResponseDto appointment,
            String changedBy,
            String reason) {
        try {
            AppointmentAuditEvent auditEvent = AppointmentAuditEvent.cancelled(
                    appointment.getId(),
                    appointment.getUser_id(),
                    appointment.getPatient_id(),
                    appointment.getDate().toString(),
                    appointment.getHour(),
                    changedBy,
                    reason);

            kafkaAuditEventPublisher.publishAuditEvent(auditEvent);
        } catch (Exception e) {
            log.error("Falha ao publicar evento de auditoria: {}", e.getMessage(), e);
        }
    }
}
