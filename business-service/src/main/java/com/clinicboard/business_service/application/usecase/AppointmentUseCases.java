package com.clinicboard.business_service.application.usecase;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.clinicboard.business_service.application.dto.AppointmentRequestDto;
import com.clinicboard.business_service.application.dto.AppointmentResponseDto;
import com.clinicboard.business_service.application.port.in.AppointmentUseCasesPort;
import com.clinicboard.business_service.application.port.out.AppointmentPersistencePort;
import com.clinicboard.business_service.application.port.out.EventPublisherGateway;
import com.clinicboard.business_service.domain.event.AppointmentScheduledEvent;

@Service
public class AppointmentUseCases implements AppointmentUseCasesPort {

    private final AppointmentPersistencePort appointmentPersistencePort;
    private final EventPublisherGateway eventPublisher;

    public AppointmentUseCases(AppointmentPersistencePort appointmentPersistencePort,
            EventPublisherGateway eventPublisher) {
        this.appointmentPersistencePort = appointmentPersistencePort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AppointmentResponseDto create(AppointmentRequestDto appointment) {
        AppointmentResponseDto appointmentResponse = appointmentPersistencePort.create(appointment);
        publishAppointmentScheduledEvent(appointmentResponse);
        return appointmentResponse;
    }

    @Override
    public List<AppointmentResponseDto> findAll() {
        return appointmentPersistencePort.findAll();
    }

    @Override
    public AppointmentResponseDto update(String id, AppointmentRequestDto appointment) {
        return appointmentPersistencePort.update(id, appointment);
    }

    @Override
    public void delete(String id) {
        appointmentPersistencePort.delete(id);
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
}
