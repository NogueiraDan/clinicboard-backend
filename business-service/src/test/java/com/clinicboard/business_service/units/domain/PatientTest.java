package com.clinicboard.business_service.units.domain;

import com.clinicboard.business_service.domain.model.Appointment;
import com.clinicboard.business_service.domain.model.AppointmentType;
import com.clinicboard.business_service.domain.model.Patient;
import com.clinicboard.business_service.domain.value_objects.ProfessionalId;
import com.clinicboard.business_service.domain.value_objects.Hour;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Patient - Testes de Domínio")
class PatientTest {

    @Test
    @DisplayName("Deve criar paciente com dados válidos")
    void deveCriarPacienteComDadosValidos() {
        // Given & When
        Patient patient = new Patient();
        patient.setName("João Silva");
        patient.setAge(30);
        patient.setEmail("joao@email.com");
        patient.setPhone("+55 11999887766");
        patient.setAdditional_info("Histórico médico relevante");
        patient.setUser_id("user-123");

        // Then
        assertNotNull(patient);
        assertEquals("João Silva", patient.getName());
        assertEquals(30, patient.getAge());
        assertEquals("joao@email.com", patient.getEmail());
        assertEquals("+55 11999887766", patient.getPhone());
        assertEquals("Histórico médico relevante", patient.getAdditional_info());
        assertEquals("user-123", patient.getUser_id());
        assertNotNull(patient.getAppointments());
        assertTrue(patient.getAppointments().isEmpty());
    }

    @Test
    @DisplayName("Deve inicializar lista de agendamentos vazia")
    void deveInicializarListaDeAgendamentosVazia() {
        // Given & When
        Patient patient = new Patient();

        // Then
        assertNotNull(patient.getAppointments());
        assertTrue(patient.getAppointments().isEmpty());
    }

    @Test
    @DisplayName("Deve permitir adicionar agendamentos ao paciente")
    void devePermitirAdicionarAgendamentosAoPaciente() {
        // Given
        Patient patient = new Patient();
        patient.setAppointments(new ArrayList<>());

        Appointment appointment1 = new Appointment();
        appointment1.setId("appt-1");
        appointment1.setDate(LocalDate.now().plusDays(1));
        appointment1.setHour(Hour.of("10:00"));
        appointment1.setType(AppointmentType.MARCACAO);
        appointment1.setPatient(patient);

        Appointment appointment2 = new Appointment();
        appointment2.setId("appt-2");
        appointment2.setDate(LocalDate.now().plusDays(2));
        appointment2.setHour(Hour.of("14:00"));
        appointment2.setType(AppointmentType.REMARCACAO);
        appointment2.setPatient(patient);

        // When
        patient.getAppointments().add(appointment1);
        patient.getAppointments().add(appointment2);

        // Then
        assertEquals(2, patient.getAppointments().size());
        assertTrue(patient.getAppointments().contains(appointment1));
        assertTrue(patient.getAppointments().contains(appointment2));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10, 150, 200})
    @DisplayName("Deve aceitar idades inválidas (validação delegada à camada de aplicação)")
    void deveAceitarIdadesInvalidas(int idade) {
        // Given & When
        Patient patient = new Patient();
        patient.setAge(idade);

        // Then
        assertEquals(idade, patient.getAge());
        // Nota: A validação de negócio deve ser feita na camada de aplicação/serviço
    }

    @Test
    @DisplayName("Deve permitir informações adicionais nulas")
    void devePermitirInformacoesAdicionaisNulas() {
        // Given & When
        Patient patient = new Patient();
        patient.setName("João Silva");
        patient.setAge(30);
        patient.setEmail("joao@email.com");
        patient.setPhone("+55 11999887766");
        patient.setAdditional_info(null);
        patient.setUser_id("user-123");

        // Then
        assertNotNull(patient);
        assertNull(patient.getAdditional_info());
        assertEquals("João Silva", patient.getName());
    }

    @Test
    @DisplayName("Deve manter integridade referencial com agendamentos")
    void deveManterIntegridadeReferencialComAgendamentos() {
        // Given
        Patient patient = new Patient();
        patient.setId("patient-123");
        patient.setAppointments(new ArrayList<>());

        Appointment appointment = new Appointment();
        appointment.setId("appt-1");
        appointment.setDate(LocalDate.now().plusDays(1));
        appointment.setHour(Hour.of("10:00"));
        appointment.setType(AppointmentType.MARCACAO);
        appointment.setProfessionalId(ProfessionalId.of("user-123"));

        // When
        appointment.setPatient(patient);
        patient.getAppointments().add(appointment);

        // Then
        assertEquals(patient, appointment.getPatient());
        assertTrue(patient.getAppointments().contains(appointment));
        assertEquals("patient-123", appointment.getPatient().getId());
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode baseado no ID")
    void deveImplementarEqualsEHashCodeBaseadoNoId() {
        // Given
        Patient patient1 = new Patient();
        patient1.setId("patient-123");
        patient1.setName("João Silva");

        Patient patient2 = new Patient();
        patient2.setId("patient-123");
        patient2.setName("João Santos"); // Nome diferente

        Patient patient3 = new Patient();
        patient3.setId("patient-456");
        patient3.setName("João Silva"); // ID diferente

        // Then
        assertEquals(patient1, patient2); // Mesmo ID
        assertNotEquals(patient1, patient3); // IDs diferentes
        assertEquals(patient1.hashCode(), patient2.hashCode()); // Mesmo hashCode para mesmo ID
    }
}