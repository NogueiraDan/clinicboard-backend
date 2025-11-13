package com.clinicboard.business_service.application.dto;

import java.time.Instant;

public record AppointmentReminderEvent(
    String appointmentId,
    String patientId,
    String professionalId,
    String date,
    String hour,
    String message,
    Instant createdAt
) {}