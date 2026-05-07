package com.sisol.salud.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopEspecialidadResponse {
    private String nombreEspecialidad;
    private long cantidadCitas;
}
