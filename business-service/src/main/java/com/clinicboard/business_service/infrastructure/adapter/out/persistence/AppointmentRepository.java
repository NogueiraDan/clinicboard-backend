package com.clinicboard.business_service.infrastructure.adapter.out.persistence;

import java.time.LocalDate;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.clinicboard.business_service.domain.model.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, String> {

    // Verifica se o paciente já tem um agendamento na mesma data
    boolean existsByPatientIdAndDate(String patientId, LocalDate date);

    // Verifica se existe um agendamento em uma data e hora específicas
    boolean existsByDateAndHour(LocalDate date, String hour);

    // Corrigindo a query para usar o campo correto da entidade
    @Query("SELECT s FROM Appointment s WHERE s.date = :date AND s.professionalId.value = :userId")
    List<Appointment> findByDateAndUserId(LocalDate date, String userId);
}
