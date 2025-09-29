package com.clinicboard.business_service.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.clinicboard.business_service.application.dto.PatientRequestDto;
import com.clinicboard.business_service.application.dto.PatientResponseDto;
import com.clinicboard.business_service.application.port.out.PatientPersistencePort;
import com.clinicboard.business_service.domain.model.Patient;
import com.clinicboard.business_service.infrastructure.adapter.in.web.exception.BusinessException;
import com.clinicboard.business_service.infrastructure.adapter.in.web.exception.CustomGenericException;
import com.clinicboard.business_service.infrastructure.adapter.out.persistence.mapper.PatientMapper;
import com.clinicboard.business_service.infrastructure.config.helpers.Utils;

@Repository
public class PatientPersistencePortImpl implements PatientPersistencePort {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientPersistencePortImpl(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    @Override
    public PatientResponseDto create(PatientRequestDto patient) {
        Optional<Patient> existingPatient = patientRepository.findByEmail(patient.getEmail());

        if (existingPatient.isPresent())
            throw new BusinessException("Este paciente já está cadastrado no sistema!");
        Patient patientDomain = patientMapper.toEntity(patient);
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
        return patientMapper.toDto(patientRepository.save(existingPatient));
    }

    @Override
    public void delete(String id) {
        patientRepository.deleteById(id);
    }

    @Override
    public List<PatientResponseDto> findPatientByName(String name) {
        return patientRepository.findByNameContaining(name).stream()
                .map(Utils::convertToPatientResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public PatientResponseDto findOne(String id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new CustomGenericException("Paciente com id " + id + " não foi encontrado"));
        return patientMapper.toDto(patient);
    }

}
