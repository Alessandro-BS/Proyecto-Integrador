package com.sisol.salud.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sisol.salud.dto.response.DisponibilidadResponse;
import com.sisol.salud.dto.response.MedicoResponse;
import com.sisol.salud.model.entity.DisponibilidadMedica;
import com.sisol.salud.model.entity.Medico;

@Mapper(componentModel = "spring") // Le decimos a MapStruct que este mapper será un componente de Spring
public interface MedicoMapper {

    // 1. Mapeo de Médico a MedicoResponse
    @Mapping(target = "nombre", source = "usuario.nombre")
    @Mapping(target = "apellido", source = "usuario.apellido")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "especialidades", source = "especialidades")
    @Mapping(target = "cpm", source = "numeroColegiatura")
    MedicoResponse toResponse(Medico medico);

    default List<String> mapEspecialidades(java.util.Set<com.sisol.salud.model.entity.Especialidad> especialidades) {
        if (especialidades == null) {
            return java.util.Collections.emptyList();
        }
        return especialidades.stream()
                .map(com.sisol.salud.model.entity.Especialidad::getNombre)
                .collect(java.util.stream.Collectors.toList());
    }

    // 2. Mapeo de lista de Médicos a lista de MedicoResponse (getAll)
    List<MedicoResponse> toResponseList(List<Medico> medicos);

    // 3. Mapeo de DisponibilidadMedica a DisponibilidadResponse
    DisponibilidadResponse toDisponibilidadResponse(DisponibilidadMedica disponibilidadMedica);

    // 4. Mapeo de lista de Disponibilidad
    List<DisponibilidadResponse> toDisponibilidadResponseList(List<DisponibilidadMedica> disponibilidades);

}
