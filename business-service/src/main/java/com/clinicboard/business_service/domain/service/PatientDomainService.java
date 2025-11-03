package com.clinicboard.business_service.domain.service;

import org.springframework.stereotype.Service;

import com.clinicboard.business_service.domain.exception.BusinessException;
import com.clinicboard.business_service.application.dto.PatientResponseDto;
import com.clinicboard.business_service.domain.model.Patient;

/**
 * Domain Service for handling patient rules.
 */
@Service
public class PatientDomainService {

    public void validatePatientExists(boolean exists) {
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

    public void validatePatientData(Patient patient) {
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            throw new BusinessException("Nome do paciente é obrigatório");
        }

        if (patient.getEmail() == null || patient.getEmail().trim().isEmpty()) {
            throw new BusinessException("Email do paciente é obrigatório");
        }

        // Outras validações de domínio
    }

    public void validateEmailUniqueness(boolean emailExists) {
        if (emailExists) {
            throw new BusinessException("Este paciente já está cadastrado no sistema!");
        }
    }

}
