package com.sisol.salud.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicoResponse {

    private Long id;
    private String nombre;
    private String apellido;
    private String cpm; // Colegio médico del Perú
    private String email;
    private String especialidad; // Devolverá solo el nombre de la especialidad

}
