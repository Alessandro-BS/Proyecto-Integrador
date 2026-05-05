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

        // Validacion 2: Revisar si ya tiene un turno ese mismo día que se cruce
        // Evitar que el médico tenga dos turnos al mismo día
        boolean yaTieneTurnoEseDia = disponibilidadRepository.findByMedicoId(medicoId).stream()
                .anyMatch(d -> d.getDiaSemana().equals(request.getDiaSemana()));

        if (yaTieneTurnoEseDia) {
            throw new RecursoDuplicadoException(
                    "El médico ya tiene disponibilidad registrada para el día " + request.getDiaSemana());
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
