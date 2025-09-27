package com.clinicboard.user_service.application.usecases;

import java.util.List;

import org.springframework.stereotype.Service;

import com.clinicboard.user_service.application.dto.UpdateUserRequestDto;
import com.clinicboard.user_service.application.dto.UserRequestDto;
import com.clinicboard.user_service.application.dto.UserResponseDto;
import com.clinicboard.user_service.application.ports.in.UserUseCasesPort;
import com.clinicboard.user_service.application.ports.out.UserPersistencePort;
import com.clinicboard.user_service.infrastructure.adapters.out.UserRepository;

@Service
public class UserUseCases implements UserUseCasesPort {

    private final UserPersistencePort userPersistencePort;

    public UserUseCases(UserPersistencePort userPersistencePort) {
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public UserResponseDto create(UserRequestDto user) {
        return userPersistencePort.create(user);
    }

    @Override
    public List<UserResponseDto> findAll() {
        return userPersistencePort.findAll();
    }

    @Override
    public UserResponseDto findById(String id) {
        return userPersistencePort.findById(id);
    }

    @Override
    public UserRepository getUserRepository() {
        return userPersistencePort.getUserRepository();
    }

    @Override
    public UserResponseDto update(String id, UpdateUserRequestDto user) {
        return userPersistencePort.update(id, user);
    }

    @Override
    public void delete(String id) {
        userPersistencePort.delete(id);
    }

    @Override
    public UserResponseDto findByEmail(String email) {
        return userPersistencePort.findByEmail(email);
    }

}
