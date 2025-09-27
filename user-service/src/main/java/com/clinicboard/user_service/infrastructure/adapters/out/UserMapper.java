package com.clinicboard.user_service.infrastructure.adapters.out;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.clinicboard.user_service.application.dto.UpdateUserRequestDto;
import com.clinicboard.user_service.application.dto.UserRequestDto;
import com.clinicboard.user_service.application.dto.UserResponseDto;
import com.clinicboard.user_service.domain.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Mapeamento de UserRequestDto para User
    @Mapping(target = "id", ignore = true) // O ID é gerado automaticamente
    @Mapping(target = "role", ignore = true) // Role será atribuída posteriormente
    @Mapping(target = "authorities", ignore = true) // Authorities serão atribuídas posteriormente
    User toEntity(UserRequestDto userRequestDto);

    // Mapeamento de User para UserResponseDto
    UserResponseDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    void updateUserFromDto(UpdateUserRequestDto dto, @MappingTarget User user);
}