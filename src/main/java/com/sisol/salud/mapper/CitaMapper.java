package com.sisol.salud.mapper;

import com.sisol.salud.dto.response.CitaResponse;
import com.sisol.salud.model.entity.Cita;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CitaMapper {

    // Extraemos el nombre completo del paciente (concatenando nombre y apellido de
    // su usuario)
    @Mapping(target = "pacienteNombre", expression = "java(cita.getPaciente().getUsuario().getNombre() + ' ' + cita.getPaciente().getUsuario().getApellido())")

    // Extraemos el nombre completo del médico (concatenando nombre y apellido de su
    // usuario)
    @Mapping(target = "medicoNombre", expression = "java(cita.getMedico().getUsuario().getNombre() + ' ' + cita.getMedico().getUsuario().getApellido())")

    // Extraemos el nombre de la especialidad
    @Mapping(target = "especialidad", source = "especialidad.nombre")

    // El estado es un Enum, lo pasamos a String
    @Mapping(target = "estado", expression = "java(cita.getEstado().name())")

    // Combina la fecha y horaInicio en LocalDateTime
    @Mapping(target = "fechaHora", expression = "java(cita.getFecha() != null && cita.getHoraInicio() != null ? cita.getFecha().atTime(cita.getHoraInicio()) : null)")
    CitaResponse toResponse(Cita cita);

    List<CitaResponse> toResponseList(List<Cita> citas);
}
