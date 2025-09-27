package com.clinicboard.user_service.infrastructure.adapters.in;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clinicboard.user_service.application.dto.LoginRequestDto;
import com.clinicboard.user_service.application.dto.LoginResponseDto;
import com.clinicboard.user_service.application.dto.UserRequestDto;
import com.clinicboard.user_service.application.dto.UserResponseDto;
import com.clinicboard.user_service.application.ports.in.UserUseCasesPort;
import com.clinicboard.user_service.domain.User;
import com.clinicboard.user_service.infrastructure.config.TokenService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserUseCasesPort useCasesPort;
    private final TokenService tokenService;

    public AuthenticationController(AuthenticationManager authenticationManager, UserUseCasesPort serviceInputPort,
            TokenService tokenServiceInputPort) {
        this.authenticationManager = authenticationManager;
        this.useCasesPort = serviceInputPort;
        this.tokenService = tokenServiceInputPort;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword());
        var auth = this.authenticationManager.authenticate(usernamePassword);
        var userData = this.useCasesPort.findByEmail(data.getEmail());
        var token = this.tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok().body(new LoginResponseDto(userData.getId(), userData.getName(), userData.getEmail(),
                userData.getRole(), token));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> create(@RequestBody @Valid UserRequestDto user) {
        if (useCasesPort.getUserRepository().findByEmail(user.getEmail()) != null)
            return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(user.getPassword());
        UserRequestDto newUser = new UserRequestDto(user.getName(), user.getEmail(), encryptedPassword,
                user.getContact(), user.getRole());

        UserResponseDto savedUser = this.useCasesPort.create(newUser);

        return ResponseEntity.ok().body(savedUser);

    }
}