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

@Mapper(componentModel = "spring")
public interface AppointmentMapper {
    AppointmentMapper INSTANCE = Mappers.getMapper(AppointmentMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patient", source = "patient_id", qualifiedByName = "uuidToPatient")
    Appointment toEntity(AppointmentRequestDto appointmentRequestDto);

    @Mapping(source = "patient.id", target = "patient_id")
    AppointmentResponseDto toDto(Appointment appointment);

    @Mapping(target = "id", ignore = true) // Ignora o ID, pois não deve ser alterado em uma atualização
    @Mapping(target = "patient", source = "patient_id", qualifiedByName = "uuidToPatient") // Converte o patient_id para o objeto Patient
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

}
