package com.clinicboard.business_service.infrastructure.adapter.in.web.exception;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
