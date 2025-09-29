package com.clinicboard.user_service.application.ports.in;

import java.util.List;

import com.clinicboard.user_service.application.dto.UpdateUserRequestDto;
import com.clinicboard.user_service.application.dto.UserRequestDto;
import com.clinicboard.user_service.application.dto.UserResponseDto;
import com.clinicboard.user_service.infrastructure.adapters.out.persistence.UserRepository;

public interface UserUseCasesPort {
    UserRepository getUserRepository();

    UserResponseDto create(UserRequestDto user);

    List<UserResponseDto> findAll();

    UserResponseDto findById(String id);

    UserResponseDto findByEmail(String email);

    UserResponseDto update(String id, UpdateUserRequestDto user);

    void delete(String id);

}
