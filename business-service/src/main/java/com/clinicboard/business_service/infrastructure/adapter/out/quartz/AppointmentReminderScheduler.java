package com.clinicboard.business_service.infrastructure.adapter.out.quartz;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clinicboard.business_service.application.dto.AppointmentResponseDto;

@Service
public class AppointmentReminderScheduler {

        @Autowired
        private Scheduler scheduler;

        public void scheduleReminder(AppointmentResponseDto appointment) throws SchedulerException {
                JobDetail jobDetail = JobBuilder.newJob(AppointmentReminderJob.class)
                                .withIdentity("reminder-" + appointment.getId())
                                .usingJobData("appointmentId", appointment.getId())
                                .usingJobData("patientId", appointment.getPatient_id())
                                .usingJobData("professionalId", appointment.getUser_id())
                                .usingJobData("date", appointment.getDate().toString())
                                .usingJobData("hour", appointment.getHour())
                                .build();

                // Calcula exatamente 24 horas antes do agendamento
                LocalDateTime appointmentDateTime = appointment.getDate()
                                .atTime(LocalTime.parse(appointment.getHour()));
                LocalDateTime reminderDateTime = appointmentDateTime.minusHours(24);

                // Para testes, descomente a linha abaixo para disparar em 1 minuto
                // LocalDateTime reminderDateTime = LocalDateTime.now().plusMinutes(1);

                ZonedDateTime zonedDateTime = reminderDateTime.atZone(ZoneId.of("America/Sao_Paulo"));

                Trigger trigger = TriggerBuilder.newTrigger()
                                .withIdentity("trigger-" + appointment.getId())
                                .startAt(Date.from(zonedDateTime.toInstant()))
                                .build();

                scheduler.scheduleJob(jobDetail, trigger);
        }
}