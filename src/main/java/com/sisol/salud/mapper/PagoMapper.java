package com.sisol.salud.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sisol.salud.dto.response.PagoResponse;
import com.sisol.salud.model.entity.Pago;

@Mapper(componentModel = "spring")
public interface PagoMapper {

    @Mapping(target = "citaId", source = "cita.id")
    @Mapping(target = "pacienteId", source = "paciente.id")
    PagoResponse toResponse(Pago pago);

    List<PagoResponse> toResponseList(List<Pago> pagos);
}
