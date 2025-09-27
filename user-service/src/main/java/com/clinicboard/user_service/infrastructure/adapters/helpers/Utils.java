package com.clinicboard.user_service.infrastructure.adapters.helpers;

import org.springframework.security.core.userdetails.UserDetails;

import com.clinicboard.user_service.application.dto.UserResponseDto;
import com.clinicboard.user_service.domain.User;

public class Utils {

    public static UserResponseDto convertToUserResponseDto(UserDetails userDetails) {
        if (userDetails instanceof User) {
            User user = (User) userDetails;
            return new UserResponseDto(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getContact(),
                    user.getRole());
        }
        return null; // Tratar exceção adequadamente
    }

}