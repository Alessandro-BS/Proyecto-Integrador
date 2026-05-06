package com.sisol.salud.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CitaResponse {

    private Long id;

    // Mostraremos el nombre completo para que sea más fácil para el frontend
    private String pacienteNombre;
    private String medicoNombre;

    // La especialidad para que el paciente sepa con quién se atiende
    private String especialidad;

    private LocalDateTime fechaHora;
    private String estado;
    private String motivoConsulta;
}
