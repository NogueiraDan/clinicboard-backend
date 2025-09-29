package com.clinicboard.business_service.application.usecase;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.clinicboard.business_service.application.dto.AppointmentRequestDto;
import com.clinicboard.business_service.application.dto.AppointmentResponseDto;
import com.clinicboard.business_service.application.port.in.AppointmentUseCasesPort;
import com.clinicboard.business_service.application.port.out.AppointmentPersistencePort;

@Service
public class AppointmentUseCases implements AppointmentUseCasesPort {

    private final AppointmentPersistencePort appointmentPersistencePort;

    public AppointmentUseCases(AppointmentPersistencePort appointmentPersistencePort) {
        this.appointmentPersistencePort = appointmentPersistencePort;
    }

    @Override
    // @Transactional
    public AppointmentResponseDto create(AppointmentRequestDto appointment) {
        AppointmentResponseDto appointmentResponse = appointmentPersistencePort.create(appointment);
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
    public List<AppointmentResponseDto> findSchedules(String userId, LocalDate date) {
        return appointmentPersistencePort.findSchedules(userId, date);
    }

    @Override
    public List<String> findAvailableHours(LocalDate date, String userId) {
        return appointmentPersistencePort.findAvailableHours(date, userId);
    }

}
