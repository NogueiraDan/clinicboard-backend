package com.clinicboard.business_service.application.port.out;

import java.util.List;

import com.clinicboard.business_service.application.dto.AppointmentRequestDto;
import com.clinicboard.business_service.application.dto.AppointmentResponseDto;

import java.time.LocalDate;

public interface AppointmentPersistencePort {
    AppointmentResponseDto create(AppointmentRequestDto appointment);

    List<AppointmentResponseDto> findAll();

    AppointmentResponseDto update(String id, AppointmentRequestDto appointment);

    void delete(String id);

    List<AppointmentResponseDto> findSchedules(String userId, LocalDate date);

    List<String> findAvailableHours(LocalDate date, String userId);
}
