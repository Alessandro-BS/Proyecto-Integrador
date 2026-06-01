package com.sisol.salud.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.sisol.salud.model.enums.MetodoPago;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagoRequest {

    @NotNull(message = "El ID de la cita es obligatorio")
    private Long citaId;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    private String referenciaPago;

    private String notas;
}
