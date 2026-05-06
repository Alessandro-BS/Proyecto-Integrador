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
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        // 2. Validar que el médico existe
        Medico medico = medicoRepository.findById(request.getMedicoId())
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

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
}
