package com.clinicboard.user_service.application.dto;

import com.clinicboard.user_service.domain.UserRole;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    
    private String id;
    private String name;
    private String email;
    private UserRole role;
    private String access_token;
}