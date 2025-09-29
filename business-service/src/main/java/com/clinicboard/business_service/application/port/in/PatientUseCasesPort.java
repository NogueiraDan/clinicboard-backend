package com.clinicboard.business_service.application.port.in;

import java.util.List;

import com.clinicboard.business_service.application.dto.PatientRequestDto;
import com.clinicboard.business_service.application.dto.PatientResponseDto;

public interface PatientUseCasesPort {
    PatientResponseDto create(PatientRequestDto patient);

    List<PatientResponseDto> findAll();

    List<PatientResponseDto> findByUserId(String id);

    List<PatientResponseDto> findPatientByName(String name);

    PatientResponseDto update(String id, PatientRequestDto patient);

    void delete(String id);

    PatientResponseDto findOne(String id);
}
