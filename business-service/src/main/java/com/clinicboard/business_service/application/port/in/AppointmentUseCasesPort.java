package com.clinicboard.business_service.application.port.in;

import java.time.LocalDate;
import java.util.List;

import com.clinicboard.business_service.application.dto.AppointmentRequestDto;
import com.clinicboard.business_service.application.dto.AppointmentResponseDto;

public interface AppointmentUseCasesPort {
     AppointmentResponseDto create(AppointmentRequestDto patient);

    List<AppointmentResponseDto> findAll();

    AppointmentResponseDto update(String id, AppointmentRequestDto scheduling);

    void delete(String id);

    List<AppointmentResponseDto> findAppointments(String userId, LocalDate date);

    List<String> findAvailableHours(LocalDate date, String userId);
}
