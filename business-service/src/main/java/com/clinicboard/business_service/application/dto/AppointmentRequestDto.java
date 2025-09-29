package com.clinicboard.business_service.application.dto;

import java.time.LocalDate;

import com.clinicboard.business_service.domain.model.AppointmentType;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AppointmentRequestDto {

    @NotNull(message = "Data/hora do agendamento é obrigatória")
    @Future(message = "Data/hora deve ser futura")
    private LocalDate date;

    @NotNull(message = "Hora do agendamento é obrigatória")
    private String hour;

    @NotNull(message = "Tipo do agendamento é obrigatório")
    private AppointmentType type;

    @NotNull(message = "ID do profissional é obrigatório")
    private String user_id;

    @NotNull(message = "ID do paciente é obrigatório")
    private String patient_id;

}
