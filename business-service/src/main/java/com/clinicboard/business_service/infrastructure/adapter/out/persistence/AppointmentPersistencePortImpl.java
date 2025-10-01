package com.clinicboard.business_service.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.clinicboard.business_service.application.dto.AppointmentRequestDto;
import com.clinicboard.business_service.application.dto.AppointmentResponseDto;
import com.clinicboard.business_service.application.port.out.AppointmentPersistencePort;
import com.clinicboard.business_service.domain.model.Appointment;
import com.clinicboard.business_service.infrastructure.adapter.in.web.exception.BusinessException;
import com.clinicboard.business_service.infrastructure.adapter.in.web.exception.CustomGenericException;
import com.clinicboard.business_service.infrastructure.adapter.out.persistence.mapper.AppointmentMapper;
import com.clinicboard.business_service.infrastructure.config.helpers.Utils;

@Repository
public class AppointmentPersistencePortImpl implements AppointmentPersistencePort {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentPersistencePortImpl(AppointmentRepository appointmentRepository, AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    public AppointmentResponseDto create(AppointmentRequestDto appointment) {
        // Validações de horario
        Utils.validateHourFormat(appointment.getHour());
        Utils.validateHourRange(appointment.getHour());

        // Regras de negócio de agendamentos
        checkPatientHasNoSchedulingOnSameDate(appointment.getPatient_id(), appointment.getDate());
        checkNoSchedulingOnSameDateTime(appointment.getDate(), appointment.getHour());

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

        // Validações
        Utils.validateHourFormat(appointment.getHour());
        Utils.validateHourRange(appointment.getHour());
        Utils.validateTypeUpdateScheduling(appointment.getType());
        checkNoSchedulingOnSameDateTime(appointment.getDate(), appointment.getHour());

        Appointment existingAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new CustomGenericException("Agendamento com id " + id + " não foi encontrado"));
        appointmentMapper.updateAppointmentFromDto(appointment, existingAppointment);
        return appointmentMapper.toDto(appointmentRepository.save(existingAppointment));
    }

    @Override
    public void delete(String id) {
        appointmentRepository.deleteById(id);

    }

    private void checkPatientHasNoSchedulingOnSameDate(String patientId, LocalDate date) {
        boolean exists = appointmentRepository.existsByPatientIdAndDate(patientId, date);
        if (exists) {
            throw new BusinessException("O paciente já possui um agendamento nesta data.");
        }
    }

    private void checkNoSchedulingOnSameDateTime(LocalDate date, String hour) {
        boolean exists = appointmentRepository.existsByDateAndHour(date, hour);
        if (exists) {
            throw new BusinessException("Já existe um agendamento para essa data e hora.");
        }
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
                        .anyMatch(appointment -> appointment.getHour().equals(hour)))
                .collect(Collectors.toList());

    }

}
