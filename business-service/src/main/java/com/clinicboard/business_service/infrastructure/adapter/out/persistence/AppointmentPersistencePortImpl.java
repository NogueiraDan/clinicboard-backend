package com.clinicboard.business_service.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.clinicboard.business_service.application.dto.AppointmentRequestDto;
import com.clinicboard.business_service.application.dto.AppointmentResponseDto;
import com.clinicboard.business_service.application.port.out.AppointmentPersistencePort;
import com.clinicboard.business_service.domain.model.Appointment;
import com.clinicboard.business_service.infrastructure.adapter.in.web.exception.CustomGenericException;
import com.clinicboard.business_service.infrastructure.adapter.out.persistence.mapper.AppointmentMapper;
import com.clinicboard.business_service.infrastructure.config.helpers.Utils;

@Repository
public class AppointmentPersistencePortImpl implements AppointmentPersistencePort {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentPersistencePortImpl(AppointmentRepository appointmentRepository,
            AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    public AppointmentResponseDto create(AppointmentRequestDto appointment) {

        Appointment appointmentDomain = appointmentMapper.toEntity(appointment);
        Appointment savedAppointment = appointmentRepository.save(appointmentDomain);
        return appointmentMapper.toDto(savedAppointment);
    }

    @Override
    public List<AppointmentResponseDto> findAll() {
        return appointmentRepository.findAll().stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponseDto update(String id, AppointmentRequestDto appointment) {
        Appointment existingAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new CustomGenericException("Agendamento com id " + id + " não foi encontrado"));
        appointmentMapper.updateAppointmentFromDto(appointment, existingAppointment);
        return appointmentMapper.toDto(appointmentRepository.save(existingAppointment));
    }

    @Override
    public void delete(String id) {
        appointmentRepository.deleteById(id);

    }

    @Override
    public List<AppointmentResponseDto> findAppointments(String id, LocalDate date) {
        return appointmentRepository.findByDateAndUserId(date, id).stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findAvailableHours(LocalDate date, String userId) {
        List<Appointment> appointments = appointmentRepository.findByDateAndUserId(date, userId);

        List<String> allHours = Utils.generateAllHours();

        return allHours.stream()
                .filter(hour -> !appointments.stream()
                        .anyMatch(appointment -> appointment.getHour().value().equals(hour)))
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentRepository getAppointmentRepository() {
        return this.appointmentRepository;
    }

}
