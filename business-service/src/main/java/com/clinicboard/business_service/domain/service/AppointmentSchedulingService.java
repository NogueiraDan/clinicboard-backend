package com.clinicboard.business_service.domain.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.clinicboard.business_service.application.port.out.AppointmentPersistencePort;
import com.clinicboard.business_service.domain.exception.BusinessException;
import com.clinicboard.business_service.domain.model.AppointmentType;

/**
 * Domain Service for handling appointment scheduling rules.
 */
@Service
public class AppointmentSchedulingService {

    private final AppointmentPersistencePort appointmentPersistencePort;

    public AppointmentSchedulingService(AppointmentPersistencePort appointmentPersistencePort) {
        this.appointmentPersistencePort = appointmentPersistencePort;
    }

    public void checkPatientHasNoSchedulingOnSameDate(String patientId, LocalDate date) {
        boolean exists = appointmentPersistencePort.getAppointmentRepository().existsByPatientIdAndDate(patientId,
                date);
        if (exists) {
            throw new BusinessException("O paciente já possui um agendamento nesta data.");
        }
    }

    public void checkNoSchedulingOnSameDateTime(LocalDate date, String hour) {
        boolean exists = appointmentPersistencePort.getAppointmentRepository().existsByDateAndHour(date, hour);
        if (exists) {
            throw new BusinessException("Já existe um agendamento para essa data e hora.");
        }
    }

    public void validateTypeUpdateScheduling(AppointmentType type) {
        if (type != AppointmentType.REMARCACAO) {
            throw new BusinessException("Tipo de agendamento inválido para atualização");
        }
    }

}
