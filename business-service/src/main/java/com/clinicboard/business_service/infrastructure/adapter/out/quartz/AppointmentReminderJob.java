package com.clinicboard.business_service.infrastructure.adapter.out.quartz;

import org.quartz.JobExecutionContext;

import java.time.Instant;

import org.quartz.JobDataMap;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.clinicboard.business_service.application.dto.AppointmentReminderEvent;
import com.clinicboard.business_service.application.port.out.EventPublisherGateway;

public class AppointmentReminderJob extends QuartzJobBean {

    private final EventPublisherGateway eventPublisher;

    public AppointmentReminderJob(EventPublisherGateway eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String appointmentId = dataMap.getString("appointmentId");
        String patientId = dataMap.getString("patientId");
        String professionalId = dataMap.getString("professionalId");
        String date = dataMap.getString("date");
        String hour = dataMap.getString("hour");

        String message = String.format("Lembrete: Seu atendimento é amanhã às %s", hour);

        AppointmentReminderEvent reminderEvent = new AppointmentReminderEvent(
                appointmentId, patientId, professionalId, date, hour, message, Instant.now());

        eventPublisher.publishAppointmentReminderNotification(reminderEvent);
    }
}