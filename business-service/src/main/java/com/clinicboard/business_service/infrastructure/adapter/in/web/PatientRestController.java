package com.clinicboard.business_service.infrastructure.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicboard.business_service.application.dto.PatientRequestDto;
import com.clinicboard.business_service.application.dto.PatientResponseDto;
import com.clinicboard.business_service.application.port.in.PatientUseCasesPort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("patients")
public class PatientRestController {

    private final PatientUseCasesPort patientUseCasesPort;

    public PatientRestController(PatientUseCasesPort patientUseCasesPort) {
        this.patientUseCasesPort = patientUseCasesPort;
    }

    @GetMapping()
    public List<PatientResponseDto> findAll() {
        return patientUseCasesPort.findAll();
    }

    @GetMapping("/patient/{id}")
    public PatientResponseDto findOne(@PathVariable String id) {
        return patientUseCasesPort.findOne(id);
    }

    @GetMapping("/user/{userId}")
    public List<PatientResponseDto> findByUserId(@PathVariable String userId) {
        return patientUseCasesPort.findByUserId(userId);
    }

    @GetMapping("/patient")
    public List<PatientResponseDto> findPatientByName(@RequestParam String name) {
        return patientUseCasesPort.findPatientByName(name);
    }

    @PostMapping()
    public ResponseEntity<PatientResponseDto> create(@RequestBody PatientRequestDto patient) {
        PatientResponseDto newPatient = patientUseCasesPort.create(patient);
        return ResponseEntity.ok(newPatient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDto> update(@PathVariable String id,
            @RequestBody PatientRequestDto updateUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientUseCasesPort.update(id, updateUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        patientUseCasesPort.delete(id);
        return ResponseEntity.noContent().build();
    }

}
