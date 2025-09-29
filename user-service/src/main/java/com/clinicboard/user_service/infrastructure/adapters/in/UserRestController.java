package com.clinicboard.user_service.infrastructure.adapters.in;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.clinicboard.user_service.application.dto.UpdateUserRequestDto;
import com.clinicboard.user_service.application.dto.UserResponseDto;
import com.clinicboard.user_service.application.ports.in.UserUseCasesPort;
import com.clinicboard.user_service.application.ports.out.BusinessPort;

@RestController
@RequestMapping("users")
public class UserRestController {

    private final UserUseCasesPort userUseCasesPort;
    private final BusinessPort businessPort;

    public UserRestController(UserUseCasesPort userUseCasesPort, BusinessPort businessPort) {
        this.userUseCasesPort = userUseCasesPort;
        this.businessPort = businessPort;
    }

    @GetMapping()
    public List<UserResponseDto> findAll() {
        return userUseCasesPort.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findById(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK).body(userUseCasesPort.findById(id));
    }

    @GetMapping("/user/{email}")
    public UserResponseDto findUserByEmail(@PathVariable String email) {
        return userUseCasesPort.findByEmail(email);
    }

    @GetMapping("/business/patients/{userId}")
    public List<Optional<?>> findPatients(@PathVariable String userId) {
        return businessPort.findByUserId(userId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(@PathVariable String id,
            @RequestBody UpdateUserRequestDto updateUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userUseCasesPort.update(id, updateUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userUseCasesPort.delete(id);
        return ResponseEntity.noContent().build();
    }

}