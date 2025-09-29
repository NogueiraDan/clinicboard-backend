package com.clinicboard.business_service.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PatientRequestDto {
    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "Idade é obrigatória")
    private Integer age;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    private String phone;

    private String additional_info;

    @NotBlank(message = "ID do profissional é obrigatório")
    private String user_id;
}
