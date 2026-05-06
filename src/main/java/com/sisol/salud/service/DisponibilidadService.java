package com.sisol.salud.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisol.salud.dto.request.DisponibilidadRequest;
import com.sisol.salud.dto.response.DisponibilidadResponse;
import com.sisol.salud.exception.RecursoDuplicadoException;
import com.sisol.salud.mapper.MedicoMapper;
import com.sisol.salud.model.entity.DisponibilidadMedica;
import com.sisol.salud.model.entity.Medico;
import com.sisol.salud.repository.DisponibilidadMedicaRepository;
import com.sisol.salud.repository.MedicoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DisponibilidadService {
    private final DisponibilidadMedicaRepository disponibilidadRepository;
    private final MedicoRepository medicoRepository;
    private final MedicoMapper medicoMapper;

    // 1. Obtener todas las disponibilidades de un médico
    public List<DisponibilidadResponse> obtenerPorMedico(Long medicoId) {
        List<DisponibilidadMedica> disponibilidades = disponibilidadRepository.findByMedicoId(medicoId);
        return medicoMapper.toDisponibilidadResponseList(disponibilidades);
    }

    // 2. Agregar un nuevo horario para un médico
    @Transactional
    public DisponibilidadResponse agregarDisponibilidad(Long medicoId, DisponibilidadRequest request) {
        Medico medico = medicoRepository.findById(medicoId)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con ID: " + medicoId));

        // Validacion 1: ¿La hora de inicio es menor a la de fin?
        if (!request.getHoraInicio().isBefore(request.getHoraFin())) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }

        // Validación 2: Duración mínima del turno
        long duracionHoras = java.time.Duration.between(request.getHoraInicio(), request.getHoraFin()).toHours();
        if (duracionHoras < 2) {
            throw new IllegalArgumentException("El bloque de disponibilidad debe ser de al menos 2 horas.");
        }

        // Validación 3: Solapamiento de horarios en el mismo día
        // Permitimos que registre varios turnos el mismo día (ej. mañana y tarde),
        // pero verificamos que no choquen entre sí.
        List<DisponibilidadMedica> disponibilidadesDelDia = disponibilidadRepository.findByMedicoId(medicoId).stream()
                .filter(d -> d.getDiaSemana().equals(request.getDiaSemana()))
                .toList();
        for (DisponibilidadMedica existente : disponibilidadesDelDia) {
            // Un solapamiento ocurre si: InicioNuevo < FinExistente AND FinNuevo >
            // InicioExistente
            if (request.getHoraInicio().isBefore(existente.getHoraFin()) &&
                    request.getHoraFin().isAfter(existente.getHoraInicio())) {
                throw new RecursoDuplicadoException("El horario se solapa con una disponibilidad ya registrada ("
                        + existente.getHoraInicio() + " - " + existente.getHoraFin() + ").");
            }
        }

        // Crear Disponibilidad
        DisponibilidadMedica nuevaDisponibilidad = DisponibilidadMedica.builder()
                .medico(medico)
                .diaSemana(request.getDiaSemana())
                .horaInicio(request.getHoraInicio())
                .horaFin(request.getHoraFin())
                .build();

        // Guardar en la DB
        DisponibilidadMedica guardada = disponibilidadRepository.save(nuevaDisponibilidad);

        return medicoMapper.toDisponibilidadResponse(guardada);
    }

    // 3. Eliminar una disponibilidad
    @Transactional
    public void eliminarDisponibilidad(Long disponibilidadId) {
        if (!disponibilidadRepository.existsById(disponibilidadId)) {
            throw new RuntimeException("Disponibilidad no encontrada con ID: " + disponibilidadId);
        }
        disponibilidadRepository.deleteById(disponibilidadId);
    }
}
