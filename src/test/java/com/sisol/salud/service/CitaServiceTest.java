package com.sisol.salud.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sisol.salud.dto.request.CitaRequest;
import com.sisol.salud.dto.response.CitaResponse;
import com.sisol.salud.dto.response.HorarioDisponibleResponse;
import com.sisol.salud.exception.RecursoDuplicadoException;
import com.sisol.salud.exception.RecursoNoEncontradoException;
import com.sisol.salud.exception.ReglaNegocioException;
import com.sisol.salud.mapper.CitaMapper;
import com.sisol.salud.model.entity.Cita;
import com.sisol.salud.model.entity.Medico;
import com.sisol.salud.model.entity.Paciente;
import com.sisol.salud.model.entity.Usuario;
import com.sisol.salud.model.enums.EstadoCita;
import com.sisol.salud.model.entity.Especialidad;
import com.sisol.salud.repository.CitaRepository;
import com.sisol.salud.repository.EspecialidadRepository;
import com.sisol.salud.repository.MedicoRepository;
import com.sisol.salud.repository.PacienteRepository;
import com.sisol.salud.service.PagoService;

@ExtendWith(MockitoExtension.class)
public class CitaServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private DisponibilidadService disponibilidadService;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private CitaMapper citaMapper;

    @Mock
    private PagoService pagoService;

    @Mock
    private EspecialidadRepository especialidadRepository;

    @InjectMocks
    private CitaService citaService;

    private Paciente paciente;
    private Medico medico;
    private Cita cita;
    private CitaRequest citaRequest;
    private CitaResponse citaResponse;
    private Usuario usuarioPaciente;
    private Usuario usuarioMedico;
    private Especialidad especialidad;

    @BeforeEach
    void setUp() {
        usuarioPaciente = new Usuario();
        usuarioPaciente.setId(1L);

        usuarioMedico = new Usuario();
        usuarioMedico.setId(2L);

        paciente = new Paciente();
        paciente.setId(1L);
        paciente.setUsuario(usuarioPaciente);

        especialidad = new Especialidad();
        especialidad.setId(1L);
        especialidad.setNombre("Cardiología");

        medico = new Medico();
        medico.setId(1L);
        medico.setUsuario(usuarioMedico);
        medico.setEspecialidades(java.util.Set.of(especialidad));

        cita = new Cita();
        cita.setId(1L);
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setEstado(EstadoCita.PENDIENTE);
        cita.setFecha(LocalDate.now().plusDays(1)); // Mañana
        cita.setHoraInicio(LocalTime.of(10, 0));

        citaRequest = new CitaRequest();
        citaRequest.setPacienteId(1L);
        citaRequest.setMedicoId(1L);
        citaRequest.setEspecialidadId(1L);
        citaRequest.setFechaHora(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(10, 0)));
        citaRequest.setMotivoConsulta("Dolor de cabeza");

        citaResponse = new CitaResponse();
        citaResponse.setId(1L);
        citaResponse.setEstado("PENDIENTE");
    }

    @Test
    void reservarCita_ConHorarioDisponible_DebeCrearCita() {
        // Arrange
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(especialidadRepository.findById(1L)).thenReturn(Optional.of(especialidad));

        HorarioDisponibleResponse slot = new HorarioDisponibleResponse(LocalTime.of(10, 0), LocalTime.of(10, 30), true);
        when(disponibilidadService.obtenerSlotsDisponibles(1L, citaRequest.getFechaHora().toLocalDate()))
                .thenReturn(Arrays.asList(slot));

        when(citaRepository.save(any(Cita.class))).thenReturn(cita);
        when(citaMapper.toResponse(cita)).thenReturn(citaResponse);

        // Act
        CitaResponse resultado = citaService.reservarCita(citaRequest);

        // Assert
        assertNotNull(resultado);
        verify(citaRepository).save(any(Cita.class));
        verify(notificacionService).enviarConfirmacionCita(cita);
    }

    @Test
    void reservarCita_ConHorarioOcupado_DebeLanzarExcepcion() {
        // Arrange
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        when(especialidadRepository.findById(1L)).thenReturn(Optional.of(especialidad));

        HorarioDisponibleResponse slot = new HorarioDisponibleResponse(LocalTime.of(10, 0), LocalTime.of(10, 30), false); // No disponible
        when(disponibilidadService.obtenerSlotsDisponibles(1L, citaRequest.getFechaHora().toLocalDate()))
                .thenReturn(Arrays.asList(slot));

        // Act & Assert
        assertThrows(RecursoDuplicadoException.class, () -> {
            citaService.reservarCita(citaRequest);
        });

        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void cancelarCita_ConAnticipacionMayorA2Horas_DebeCancelarExitosamente() {
        // Arrange
        // La cita es para mañana (más de 2 horas)
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

        // Act
        citaService.cancelarCita(1L, 1L, false);

        // Assert
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
        verify(citaRepository).save(cita);
        verify(notificacionService).enviarCancelacionCita(cita);
    }

    @Test
    void cancelarCita_ConAnticipacionMenorA2Horas_DebeLanzarExcepcion() {
        // Arrange
        // Cita para dentro de 1 hora
        cita.setFecha(LocalDate.now());
        cita.setHoraInicio(LocalTime.now().plusHours(1));
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () -> {
            citaService.cancelarCita(1L, 1L, false);
        });

        verify(citaRepository, never()).save(any(Cita.class));
    }

    @Test
    void completarCita_PorMedicoAsignado_DebeMarcarComoCompletada() {
        // Arrange
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

        // Act
        citaService.completarCita(1L, 2L, "Todo bien"); // 2L es el ID del usuarioMedico

        // Assert
        assertEquals(EstadoCita.COMPLETADA, cita.getEstado());
        assertEquals("Todo bien", cita.getObservaciones());
        verify(citaRepository).save(cita);
    }

    @Test
    void completarCita_PorMedicoNoAsignado_DebeLanzarExcepcion() {
        // Arrange
        when(citaRepository.findById(1L)).thenReturn(Optional.of(cita));

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () -> {
            citaService.completarCita(1L, 99L, "Todo bien"); // Médico equivocado
        });

        verify(citaRepository, never()).save(any(Cita.class));
    }
}
