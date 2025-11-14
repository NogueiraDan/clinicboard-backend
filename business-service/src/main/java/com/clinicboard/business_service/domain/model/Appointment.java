package com.clinicboard.business_service.domain.model;

import java.time.LocalDate;
import java.util.UUID;

import com.clinicboard.business_service.domain.value_objects.Hour;
import com.clinicboard.business_service.domain.value_objects.ProfessionalId;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private LocalDate date;

    @Embedded
    private Hour hour;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentType type;

    @Embedded
    private ProfessionalId professionalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // Construtores
    public Appointment() {
    }

    public Appointment(String id, LocalDate date, Hour hour, AppointmentType type,
            ProfessionalId professionalId, Patient patient) {
        this.id = id;
        this.date = date;
        this.hour = hour;
        this.type = type;
        this.professionalId = professionalId;
        this.patient = patient;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    // COMPORTAMENTOS DE DOMÍNIO
    public void reschedule(LocalDate newDate, Hour newHour) {
        validateRescheduleRules(newDate);
        this.date = newDate;
        this.hour = newHour;
        this.type = AppointmentType.REMARCACAO;
    }

    public boolean canBeRescheduled() {
        return LocalDate.now().isBefore(this.date.minusDays(1));
    }

    public boolean conflictsWith(LocalDate date, Hour hour) {
        return this.date.equals(date) && this.hour.equals(hour);
    }

    private void validateRescheduleRules(LocalDate newDate) {
        if (newDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Não é possível agendar para data passada");
        }
    }

}