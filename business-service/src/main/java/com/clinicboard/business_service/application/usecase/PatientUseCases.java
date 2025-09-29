package com.clinicboard.business_service.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.clinicboard.business_service.application.dto.PatientRequestDto;
import com.clinicboard.business_service.application.dto.PatientResponseDto;
import com.clinicboard.business_service.application.port.in.PatientUseCasesPort;
import com.clinicboard.business_service.application.port.out.PatientPersistencePort;

@Service
public class PatientUseCases implements PatientUseCasesPort {

    private final PatientPersistencePort patientPersistencePort;

    public PatientUseCases(PatientPersistencePort patientPersistencePort) {
        this.patientPersistencePort = patientPersistencePort;
    }

    @Override
    public PatientResponseDto create(PatientRequestDto patient) {
        return patientPersistencePort.create(patient);
    }

    @Override
    public List<PatientResponseDto> findAll() {
        return patientPersistencePort.findAll();
    }

    @Override
    public List<PatientResponseDto> findByUserId(String id) {
        return patientPersistencePort.findByUserId(id);
    }

    @Override
    public PatientResponseDto update(String id, PatientRequestDto patient) {
        return patientPersistencePort.update(id, patient);
    }

    @Override
    public void delete(String id) {
        patientPersistencePort.delete(id);
    }

    @Override
    public List<PatientResponseDto> findPatientByName(String name) {
        return patientPersistencePort.findPatientByName(name);
    }

    @Override
    public PatientResponseDto findOne(String id) {
        return patientPersistencePort.findOne(id);
    }

}
