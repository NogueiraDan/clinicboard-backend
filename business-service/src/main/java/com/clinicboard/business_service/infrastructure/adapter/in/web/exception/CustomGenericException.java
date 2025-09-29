package com.clinicboard.business_service.infrastructure.adapter.in.web.exception;

public class CustomGenericException extends RuntimeException {
    public CustomGenericException(String message) {
        super(message);
    }
}
