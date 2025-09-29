package com.clinicboard.business_service.infrastructure.adapter.in.web;

import java.util.List;
import java.time.LocalDate;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clinicboard.business_service.application.dto.AppointmentRequestDto;
import com.clinicboard.business_service.application.dto.AppointmentResponseDto;
import com.clinicboard.business_service.application.port.in.AppointmentUseCasesPort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("appointments")
public class AppointmentRestController {

    private final AppointmentUseCasesPort appointmentUseCasesPort;

    public AppointmentRestController(AppointmentUseCasesPort appointmentUseCasesPort) {
        this.appointmentUseCasesPort = appointmentUseCasesPort;
    }

    @GetMapping()
    public List<AppointmentResponseDto> findAll() {
        return appointmentUseCasesPort.findAll();
    }

    @GetMapping("/{userId}/date")
    public List<AppointmentResponseDto> findSchedules(@PathVariable String userId, @RequestParam LocalDate date) {
        return appointmentUseCasesPort.findSchedules(userId, date);
    }

    @GetMapping("/{userId}/available")
    public List<String> findAvailableHours(@PathVariable String userId, @RequestParam LocalDate date) {
        return appointmentUseCasesPort.findAvailableHours(date, userId);
    }

    @PostMapping()
    public ResponseEntity<AppointmentResponseDto> create(@RequestBody AppointmentRequestDto appointment) {
        AppointmentResponseDto newAppointment = appointmentUseCasesPort.create(appointment);
        return ResponseEntity.ok(newAppointment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponseDto> update(@PathVariable String id,
            @RequestBody AppointmentRequestDto updateAppointment) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentUseCasesPort.update(id, updateAppointment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        appointmentUseCasesPort.delete(id);
        return ResponseEntity.noContent().build();
    }

}
