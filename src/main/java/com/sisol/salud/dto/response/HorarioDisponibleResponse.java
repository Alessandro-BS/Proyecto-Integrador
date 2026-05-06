package com.sisol.salud.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HorarioDisponibleResponse {

    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean disponible; // True si está libre, False si ya alguien agendó a esa hora

}
