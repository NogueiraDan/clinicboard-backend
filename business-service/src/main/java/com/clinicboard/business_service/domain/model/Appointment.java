package com.clinicboard.business_service.domain.model;

import java.time.LocalDate;

import com.clinicboard.business_service.domain.value_objects.Hour;
import com.clinicboard.business_service.domain.value_objects.ProfessionalId;

import jakarta.persistence.AttributeOverride;
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
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "appointments")
@EqualsAndHashCode(of = "id")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private LocalDate date;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "hour", nullable = false))
    private Hour hour;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AppointmentType type;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "user_id", nullable = false))
    private ProfessionalId professionalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
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

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Hour getHour() {
        return hour;
    }

    public void setHour(Hour hour) {
        this.hour = hour;
    }

    public AppointmentType getType() {
        return type;
    }

    public void setType(AppointmentType type) {
        this.type = type;
    }

    public ProfessionalId getProfessionalId() {
        return professionalId;
    }

    public void setProfessionalId(ProfessionalId professionalId) {
        this.professionalId = professionalId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

     // Método de conveniência para compatibilidade
    public void setHour(String hourString) {
        this.hour = Hour.of(hourString);
    }
    
    public String getHourValue() {
        return hour != null ? hour.value() : null;
    }
}