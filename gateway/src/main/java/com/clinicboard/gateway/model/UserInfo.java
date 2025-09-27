package com.clinicboard.gateway.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class UserInfo {
    private final String userId;
    private final String email;
    private final String role;
    private final String name;
    private final String contact;
    private final Instant cachedAt;

    @JsonCreator
    public UserInfo(
            @JsonProperty("userId") String userId,
            @JsonProperty("email") String email,
            @JsonProperty("role") String role,
            @JsonProperty("name") String name,
            @JsonProperty("contact") String contact,
            @JsonProperty("cachedAt") Instant cachedAt) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.name = name;
        this.contact = contact; 
        this.cachedAt = cachedAt != null ? cachedAt : Instant.now();
    }

    // Getters
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getName() { return name; }
    public String getContact() { return contact; }
    public Instant getCachedAt() { return cachedAt; }
    
    public boolean isExpired(long ttlSeconds) {
        return Instant.now().isAfter(cachedAt.plusSeconds(ttlSeconds));
    }
}