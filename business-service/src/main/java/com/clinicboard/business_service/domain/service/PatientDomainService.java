package com.clinicboard.business_service.domain.service;

import org.springframework.stereotype.Service;

import com.clinicboard.business_service.application.port.out.PatientPersistencePort;
import com.clinicboard.business_service.domain.exception.BusinessException;
import com.clinicboard.business_service.application.dto.PatientResponseDto;
import com.clinicboard.business_service.domain.model.Patient;

/**
 * Domain Service for handling patient rules.
 */
@Service
public class PatientDomainService {

    private final PatientPersistencePort patientPersistencePort;

    public PatientDomainService(PatientPersistencePort patientPersistencePort) {
        this.patientPersistencePort = patientPersistencePort;
    }

    public void checkPatientExists(String patientId) {
        boolean exists = patientPersistencePort.getPatientRepository().existsById(patientId);
        if (!exists) {
            throw new BusinessException("Paciente não encontrado.");
        }
    }

    public PatientResponseDto convertToPatientResponseDto(Patient patientDetails) {
        if (patientDetails instanceof Patient) {
            Patient patient = (Patient) patientDetails;
            return new PatientResponseDto(patient.getId(), patient.getName(), patient.getAge(), patient.getEmail(),
                    patient.getPhone(), patient.getAdditional_info(), patient.getUser_id());
        }
        return null;
    }

}
