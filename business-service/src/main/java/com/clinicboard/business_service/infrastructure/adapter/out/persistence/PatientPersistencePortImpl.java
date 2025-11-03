package com.clinicboard.business_service.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.clinicboard.business_service.application.dto.PatientRequestDto;
import com.clinicboard.business_service.application.dto.PatientResponseDto;
import com.clinicboard.business_service.application.port.out.PatientPersistencePort;
import com.clinicboard.business_service.domain.model.Patient;
import com.clinicboard.business_service.domain.service.PatientDomainService;
import com.clinicboard.business_service.infrastructure.adapter.in.web.exception.CustomGenericException;
import com.clinicboard.business_service.infrastructure.adapter.out.persistence.mapper.PatientMapper;

@Repository
public class PatientPersistencePortImpl implements PatientPersistencePort {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final PatientDomainService patientDomainService;

    public PatientPersistencePortImpl(PatientRepository patientRepository, PatientMapper patientMapper,
            PatientDomainService patientDomainService) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
        this.patientDomainService = patientDomainService;
    }

    @Override
    public PatientResponseDto create(PatientRequestDto patient) {
        Optional<Patient> existingPatient = patientRepository.findByEmail(patient.getEmail());

        patientDomainService.validateEmailUniqueness(existingPatient.isPresent());

        Patient patientDomain = patientMapper.toEntity(patient);

        patientDomainService.validatePatientData(patientDomain);

        Patient savedPatient = patientRepository.save(patientDomain);
        return patientMapper.toDto(savedPatient);
    }

    @Override
    public List<PatientResponseDto> findAll() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PatientResponseDto> findByUserId(String id) {
        return patientRepository.findByUserId(id).stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PatientResponseDto update(String id, PatientRequestDto patient) {
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new CustomGenericException("Paciente com id " + id + " não foi encontrado"));
        patientMapper.updateUserFromDto(patient, existingPatient);

        patientDomainService.validatePatientData(existingPatient);

        return patientMapper.toDto(patientRepository.save(existingPatient));
    }

    @Override
    public void delete(String id) {
        patientRepository.deleteById(id);
    }

    @Override
    public List<PatientResponseDto> findPatientByName(String name) {
        return patientRepository.findByNameContaining(name).stream()
                .map(patientDomainService::convertToPatientResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PatientResponseDto findOne(String id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new CustomGenericException("Paciente com id " + id + " não foi encontrado"));
        return patientMapper.toDto(patient);
    }

    @Override
    public PatientRepository getPatientRepository() {
        return this.patientRepository;
    }
}
