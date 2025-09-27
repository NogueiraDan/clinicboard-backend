package com.clinicboard.user_service.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    @NotBlank(message = "Email não pode ser vazio!")
    @Email(message = "Digite um email válido")
    private String email;

    @NotBlank(message = "Senha não pode ser vazia!")
    private String password;
}