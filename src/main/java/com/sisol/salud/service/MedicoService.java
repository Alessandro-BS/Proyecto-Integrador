package com.sisol.salud.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sisol.salud.dto.response.MedicoResponse;
import com.sisol.salud.mapper.MedicoMapper;
import com.sisol.salud.model.entity.Medico;
import com.sisol.salud.repository.MedicoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final MedicoMapper medicoMapper;

    // 1. Listar todos los médicos activos
    public List<MedicoResponse> obtenerTodos() {
        List<Medico> medicos = medicoRepository.findAll();
        return medicoMapper.toResponseList(medicos);
    }

    // 2. Buscar médicos por el ID de su especialidad
    public List<MedicoResponse> obtenerPorEspecialidad(Long especialidadId) {
        List<Medico> medicos = medicoRepository.findByEspecialidadId(especialidadId);
        return medicoMapper.toResponseList(medicos);
    }

    // 3. Buscar médico específico por su ID
    public MedicoResponse obtenerPorId(Long id) {
        Medico medico = medicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado con ID: " + id));
        return medicoMapper.toResponse(medico);
    }
}
