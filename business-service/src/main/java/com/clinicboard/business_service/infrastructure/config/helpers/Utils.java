package com.clinicboard.business_service.infrastructure.config.helpers;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.clinicboard.business_service.application.dto.AppointmentResponseDto;
import com.clinicboard.business_service.application.dto.PatientResponseDto;
import com.clinicboard.business_service.domain.model.Appointment;
import com.clinicboard.business_service.domain.model.AppointmentType;
import com.clinicboard.business_service.domain.model.Patient;
import com.clinicboard.business_service.infrastructure.adapter.in.web.exception.BusinessException;

public class Utils {
    public static PatientResponseDto convertToPatientResponseDto(Patient patientDetails) {
        if (patientDetails instanceof Patient) {
            Patient patient = (Patient) patientDetails;
            return new PatientResponseDto(patient.getId(), patient.getName(), patient.getAge(), patient.getEmail(),
                    patient.getPhone(), patient.getAdditional_info(), patient.getUser_id());
        }
        return null; // Tratar exceção adequadamente
    }

    public static AppointmentResponseDto convertToSchedulingResponseDto(Appointment schedulingDetails) {
        if (schedulingDetails instanceof Appointment) {
            Appointment scheduling = (Appointment) schedulingDetails;
            return new AppointmentResponseDto(scheduling.getId(), scheduling.getDate(), scheduling.getHour(),
                    scheduling.getType(), scheduling.getUser_id(), scheduling.getPatient().getId());
        }
        return null; // Tratar exceção adequadamente
    }

    public static void validateHourFormat(String hour) {
        if (!hour.matches("^([01]\\d|2[0-3]):([0-5]\\d)$")) {
            throw new BusinessException("O formato da hora deve ser HH:mm");
        }
    }

    public static void validateHourRange(String hour) {
        try {
            LocalTime time = LocalTime.parse(hour);
            LocalTime start = LocalTime.of(8, 0);
            LocalTime end = LocalTime.of(21, 0);

            if (time.isBefore(start) || time.isAfter(end)) {
                throw new BusinessException("O horário deve estar entre 08:00 e 21:00");
            }
        } catch (DateTimeParseException e) {
            throw new BusinessException("O formato da hora deve ser HH:mm");
        }
    }

    public static void validateEmailFormat(String email) {
        if (!email.matches("^[\\w-]+(\\.[\\w-]+)*@([\\w-]+\\.)+[a-zA-Z]{2,7}$")) {
            throw new BusinessException("O formato do e-mail é inválido");
        }
    }

    public static void validatePhoneNumberFormat(String phone) {
        if (!phone.matches("^\\+[0-9]{1,3}\\s[0-9]{1,15}$")) {
            throw new BusinessException("O formato do número de telefone é inválido");
        }
    }

    public static void validateZipCodeFormat(String zipCode) {
        if (!zipCode.matches("^\\d{5}-\\d{3}$")) {
            throw new BusinessException("O formato do CEP é inválido");
        }
    }

    public static void validateTypeUpdateScheduling(AppointmentType type) {
        if (type != AppointmentType.REMARCACAO) {
            throw new BusinessException("Tipo de agendamento inválido para atualização");
        }
    }

    public static List<String> generateAllHours() {
        List<String> hours = new ArrayList<>();
        for (int i = 8; i <= 21; i++) {
            String hour = String.format("%02d:00", i);
            hours.add(hour);
        }
        return hours;
    }

}
