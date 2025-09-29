package com.clinicboard.user_service.infrastructure.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import com.clinicboard.user_service.domain.User;


public interface UserRepository extends JpaRepository<User, String> {

    UserDetails findByEmail(String email);
}