package com.clinicboard.business_service.units.domain;

import com.clinicboard.business_service.domain.model.Appointment;
import com.clinicboard.business_service.domain.model.AppointmentType;
import com.clinicboard.business_service.domain.model.Patient;
import com.clinicboard.business_service.domain.value_objects.ProfessionalId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Appointment - Testes de Domínio")
class AppointmentTest {

    @Test
    @DisplayName("Deve criar agendamento com dados válidos")
    void deveCriarAgendamentoComDadosValidos() {
        // Given
        Patient patient = new Patient();
        patient.setId("patient-123");
        patient.setName("João Silva");

        // When
        Appointment appointment = new Appointment();
        appointment.setDate(LocalDate.now().plusDays(1));
        appointment.setHour("10:00");
        appointment.setType(AppointmentType.MARCACAO);
        appointment.setProfessionalId(ProfessionalId.of("user-123"));
        appointment.setPatient(patient);

        // Then
        assertNotNull(appointment);
        assertEquals(LocalDate.now().plusDays(1), appointment.getDate());
        assertEquals("10:00", appointment.getHour().toString());
        assertEquals(AppointmentType.MARCACAO, appointment.getType());
        assertEquals(ProfessionalId.of("user-123"), appointment.getProfessionalId());
        assertEquals(patient, appointment.getPatient());
    }

    @ParameterizedTest
    @EnumSource(AppointmentType.class)
    @DisplayName("Deve aceitar todos os tipos de agendamento válidos")
    void deveAceitarTodosTiposDeAgendamentoValidos(AppointmentType type) {
        // Given & When
        Appointment appointment = new Appointment();
        appointment.setType(type);

        // Then
        assertEquals(type, appointment.getType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00"})
    @DisplayName("Deve aceitar horários válidos do funcionamento")
    void deveAceitarHorariosValidosDoFuncionamento(String hora) {
        // Given & When
        Appointment appointment = new Appointment();
        appointment.setHour(hora);

        // Then
        assertEquals(hora, appointment.getHour().toString());
    }

    @Test
    @DisplayName("Deve manter associação bidirecional com paciente")
    void deveManterAssociacaoBidirecionalComPaciente() {
        // Given
        Patient patient = new Patient();
        patient.setId("patient-123");
        patient.setName("João Silva");

        Appointment appointment = new Appointment();
        appointment.setId("appt-456");
        appointment.setDate(LocalDate.now().plusDays(1));
        appointment.setHour("10:00");
        appointment.setType(AppointmentType.MARCACAO);

        // When
        appointment.setPatient(patient);

        // Then
        assertEquals(patient, appointment.getPatient());
        assertEquals("patient-123", appointment.getPatient().getId());
        assertEquals("João Silva", appointment.getPatient().getName());
    }

    @Test
    @DisplayName("Deve permitir agendamento para datas futuras")
    void devePermitirAgendamentoParaDatasFuturas() {
        // Given
        LocalDate dataFutura = LocalDate.now().plusDays(7);

        // When
        Appointment appointment = new Appointment();
        appointment.setDate(dataFutura);

        // Then
        assertEquals(dataFutura, appointment.getDate());
        assertTrue(appointment.getDate().isAfter(LocalDate.now()));
    }

    @Test
    @DisplayName("Deve aceitar datas passadas (validação de negócio delegada)")
    void deveAceitarDatasPassadas() {
        // Given
        LocalDate dataPassada = LocalDate.now().minusDays(1);

        // When
        Appointment appointment = new Appointment();
        appointment.setDate(dataPassada);

        // Then
        assertEquals(dataPassada, appointment.getDate());
        // Nota: Validação de regra de negócio deve ser feita na camada de aplicação
    }

    @Test
    @DisplayName("Deve implementar equals e hashCode baseado no ID")
    void deveImplementarEqualsEHashCodeBaseadoNoId() {
        // Given
        Appointment appointment1 = new Appointment();
        appointment1.setId("appt-123");
        appointment1.setHour("10:00");

        Appointment appointment2 = new Appointment();
        appointment2.setId("appt-123");
        appointment2.setHour("14:00"); // Hora diferente

        Appointment appointment3 = new Appointment();
        appointment3.setId("appt-456");
        appointment3.setHour("10:00"); // ID diferente

        // Then
        assertEquals(appointment1, appointment2); // Mesmo ID
        assertNotEquals(appointment1, appointment3); // IDs diferentes
        assertEquals(appointment1.hashCode(), appointment2.hashCode()); // Mesmo hashCode para mesmo ID
    }

    @Test
    @DisplayName("Deve manter referência ao profissional via user_id")
    void deveManterReferenciaAoProfissionalViaUserId() {
        // Given
        String userId = "professional-123";

        // When
        Appointment appointment = new Appointment();
        appointment.setProfessionalId(ProfessionalId.of(userId));

        // Then
        assertEquals(userId, appointment.getProfessionalId().value());
    }

    @Test
    @DisplayName("Deve permitir loading lazy do paciente")
    void devePermitirLoadingLazyDoPaciente() {
        // Given
        Appointment appointment = new Appointment();
        
        // When - Inicialmente null (lazy loading)
        appointment.setPatient(null);

        // Then
        assertNull(appointment.getPatient());
        
        // When - Associa paciente
        Patient patient = new Patient();
        patient.setId("patient-123");
        appointment.setPatient(patient);

        // Then
        assertNotNull(appointment.getPatient());
        assertEquals("patient-123", appointment.getPatient().getId());
    }

    @Test
    @DisplayName("Deve representar agendamento completo com todos os dados")
    void deveRepresentarAgendamentoCompletoComTodosOsDados() {
        // Given
        Patient patient = new Patient();
        patient.setId("patient-123");
        patient.setName("João Silva");
        patient.setEmail("joao@email.com");

        LocalDate dataAgendamento = LocalDate.now().plusDays(1);

        // When
        Appointment appointment = new Appointment();
        appointment.setId("appt-789");
        appointment.setDate(dataAgendamento);
        appointment.setHour("15:00");
        appointment.setType(AppointmentType.REMARCACAO);
        appointment.setProfessionalId(ProfessionalId.of("professional-456"));
        appointment.setPatient(patient);

        // Then
        assertNotNull(appointment);
        assertEquals("appt-789", appointment.getId());
        assertEquals(dataAgendamento, appointment.getDate());
        assertEquals("15:00", appointment.getHour().toString());
        assertEquals(AppointmentType.REMARCACAO, appointment.getType());
        assertEquals(ProfessionalId.of("professional-456"), appointment.getProfessionalId());
        assertEquals(patient, appointment.getPatient());
        assertEquals("João Silva", appointment.getPatient().getName());
    }
}