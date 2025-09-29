package com.clinicboard.business_service.application.dto;

import java.time.LocalDate;

import com.clinicboard.business_service.domain.model.AppointmentType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseDto {
    private String id;
    private LocalDate date;
    private String hour;
    private AppointmentType type;
    private String user_id;
    private String patient_id;
}
