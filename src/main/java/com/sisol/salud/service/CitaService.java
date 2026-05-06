package com.sisol.salud.service;

import com.sisol.salud.dto.request.CitaRequest;
import com.sisol.salud.dto.response.CitaResponse;
import com.sisol.salud.dto.response.HorarioDisponibleResponse;
import com.sisol.salud.exception.RecursoDuplicadoException;
import com.sisol.salud.mapper.CitaMapper;
import com.sisol.salud.model.entity.Cita;
import com.sisol.salud.model.entity.Medico;
import com.sisol.salud.model.entity.Paciente;
import com.sisol.salud.model.enums.EstadoCita;
import com.sisol.salud.repository.CitaRepository;
import com.sisol.salud.exception.RecursoNoEncontradoException;
import com.sisol.salud.exception.ReglaNegocioException;
import com.sisol.salud.repository.MedicoRepository;
import com.sisol.salud.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final DisponibilidadService disponibilidadService;
    private final CitaMapper citaMapper;

    @Transactional
    public CitaResponse reservarCita(CitaRequest request) {

        // 1. Validar que el paciente existe
        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado"));

        // 2. Validar que el médico existe
        Medico medico = medicoRepository.findById(request.getMedicoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Médico no encontrado"));

        // Extraer fecha y hora de la solicitud
        LocalDate fechaDeseada = request.getFechaHora().toLocalDate();
        LocalTime horaDeseada = request.getFechaHora().toLocalTime();

        // 3. Validar que la hora seleccionada esté realmente DISPONIBLE
        List<HorarioDisponibleResponse> slotsDelDia = disponibilidadService.obtenerSlotsDisponibles(medico.getId(),
                fechaDeseada);

        boolean esHorarioValido = slotsDelDia.stream()
                .anyMatch(slot -> slot.getHoraInicio().equals(horaDeseada) && slot.isDisponible());

        if (!esHorarioValido) {
            throw new RecursoDuplicadoException(
                    "El horario seleccionado no está disponible o el médico no atiende en ese momento.");
        }

        // 4. Crear la entidad Cita
        Cita nuevaCita = Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .fecha(fechaDeseada)
                .horaInicio(horaDeseada)
                .horaFin(horaDeseada.plusMinutes(30)) // Cada turno dura 30 minutos
                .estado(EstadoCita.PENDIENTE)
                .motivoConsulta(request.getMotivoConsulta())
                .build();

        // 5. Guardar en BD y devolver DTO
        Cita citaGuardada = citaRepository.save(nuevaCita);

        return citaMapper.toResponse(citaGuardada);
    }

    @Transactional
    public void cancelarCita(Long citaId, Long usuarioQueCancelaId, boolean esAdmin) {
        
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));

        // Validar que la cita no esté ya cancelada o completada
        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden cancelar citas pendientes");
        }

        // Regla de Negocio: Si el que cancela NO es un administrador (es un paciente), 
        // debe avisar con al menos 2 horas de anticipación
        if (!esAdmin) {
            // Unimos fecha y hora de la cita para compararlas con la fecha y hora ACTUAL
            java.time.LocalDateTime fechaHoraCita = cita.getFecha().atTime(cita.getHoraInicio());
            java.time.LocalDateTime ahora = java.time.LocalDateTime.now();

            long horasDeDiferencia = java.time.Duration.between(ahora, fechaHoraCita).toHours();

            if (horasDeDiferencia < 2) {
                throw new ReglaNegocioException("No se puede cancelar una cita con menos de 2 horas de anticipación. Por favor comuníquese con la clínica.");
            }
            
            // Validar que el paciente que cancela es el dueño de la cita
            if (!cita.getPaciente().getUsuario().getId().equals(usuarioQueCancelaId)) {
                throw new ReglaNegocioException("No tienes permisos para cancelar esta cita");
            }
        }

        // Si pasa todas las validaciones, cambiamos el estado
        cita.setEstado(EstadoCita.CANCELADA);
        citaRepository.save(cita);
    }

    // ----------------------------------------------------------------------------------
    // TAREA 4.6: FUNCIONES DEL MÉDICO (Completar cita o marcar como Inasistencia)
    // ----------------------------------------------------------------------------------

    @Transactional
    public void completarCita(Long citaId, Long medicoId, String observaciones) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));

        // Validar que el médico que está completando la cita sea el médico asignado a ella
        if (!cita.getMedico().getUsuario().getId().equals(medicoId)) {
            throw new ReglaNegocioException("No tienes permisos para modificar esta cita");
        }

        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden completar citas en estado PENDIENTE");
        }

        // Marcar como completada y guardar el diagnóstico/observaciones
        cita.setEstado(EstadoCita.COMPLETADA);
        cita.setObservaciones(observaciones);
        
        citaRepository.save(cita);
    }

    @Transactional
    public void marcarComoNoAsistio(Long citaId, Long medicoId) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada"));

        // Validar que el médico que modifica sea el asignado
        if (!cita.getMedico().getUsuario().getId().equals(medicoId)) {
            throw new ReglaNegocioException("No tienes permisos para modificar esta cita");
        }

        if (cita.getEstado() != EstadoCita.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden modificar citas en estado PENDIENTE");
        }

        cita.setEstado(EstadoCita.NO_ASISTIO);
        citaRepository.save(cita);
    }
}
