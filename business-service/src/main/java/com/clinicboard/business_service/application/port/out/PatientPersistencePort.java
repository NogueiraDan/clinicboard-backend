package com.clinicboard.business_service.application.port.out;

import java.util.List;

import com.clinicboard.business_service.application.dto.PatientRequestDto;
import com.clinicboard.business_service.application.dto.PatientResponseDto;
import com.clinicboard.business_service.infrastructure.adapter.out.persistence.PatientRepository;


public interface PatientPersistencePort {

    PatientResponseDto create(PatientRequestDto patient);
    
    List<PatientResponseDto> findAll();

    List<PatientResponseDto> findByUserId(String id);

    List<PatientResponseDto> findPatientByName(String name);

    PatientResponseDto update(String id, PatientRequestDto patient);

    void delete(String id);

    PatientResponseDto findOne(String id);

    PatientRepository getPatientRepository();
}
