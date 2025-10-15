package com.clinicboard.business_service.infrastructure.adapter.out.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.clinicboard.business_service.application.dto.AppointmentRequestDto;
import com.clinicboard.business_service.application.dto.AppointmentResponseDto;
import com.clinicboard.business_service.domain.model.Appointment;
import com.clinicboard.business_service.domain.model.Patient;
import com.clinicboard.business_service.domain.value_objects.ProfessionalId;
import com.clinicboard.business_service.domain.value_objects.Hour;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    AppointmentMapper INSTANCE = Mappers.getMapper(AppointmentMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patient", source = "patient_id", qualifiedByName = "uuidToPatient")
    @Mapping(target = "hour", source = "hour", qualifiedByName = "stringToHour")
    @Mapping(target = "professionalId", source = "user_id", qualifiedByName = "stringToProfessionalId")
    Appointment toEntity(AppointmentRequestDto appointmentRequestDto);

    @Mapping(source = "patient.id", target = "patient_id")
    @Mapping(source = "professionalId.value", target = "user_id")
    @Mapping(source = "hour", target = "hour", qualifiedByName = "hourToString")
    AppointmentResponseDto toDto(Appointment appointment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patient", source = "patient_id", qualifiedByName = "uuidToPatient")
    @Mapping(target = "professionalId", source = "user_id", qualifiedByName = "stringToProfessionalId")
    @Mapping(target = "hour", source = "hour", qualifiedByName = "stringToHour")
    void updateAppointmentFromDto(AppointmentRequestDto dto, @MappingTarget Appointment appointment);

    @Named("uuidToPatient")
    default Patient uuidToPatient(String patientId) {
        if (patientId == null) {
            return null;
        }
        Patient patient = new Patient();
        patient.setId(patientId);
        return patient;
    }

    @Named("stringToProfessionalId")
    default ProfessionalId stringToProfessionalId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return null;
        }
        return new ProfessionalId(userId);
    }

    @Named("stringToHour")
    default Hour stringToHour(String hourString) {
        if (hourString == null || hourString.trim().isEmpty()) {
            return null;
        }
        return Hour.of(hourString);
    }

    @Named("hourToString")
    default String hourToString(Hour hour) {
        return hour != null ? hour.value() : null;
    }

    default String professionalIdToString(ProfessionalId professionalId) {
        return professionalId != null ? professionalId.value() : null;
    }
}