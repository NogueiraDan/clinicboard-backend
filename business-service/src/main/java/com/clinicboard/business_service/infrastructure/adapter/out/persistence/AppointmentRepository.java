package com.clinicboard.business_service.infrastructure.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clinicboard.business_service.domain.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, String> {

    // Verifica se o paciente já tem um agendamento na mesma data
    boolean existsByPatientIdAndDate(String patientId, LocalDate date);

    // Verifica se existe um agendamento em uma data e hora específicas
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a WHERE a.date = :date AND a.hour.value = :hour")
    boolean existsByDateAndHour(@Param("date") LocalDate date, @Param("hour") String hour);

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.patient WHERE a.date = :date AND a.professionalId.value = :userId")
    List<Appointment> findByDateAndUserId(@Param("date") LocalDate date, @Param("userId") String userId);
}
