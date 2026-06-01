package com.sisol.salud.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.sisol.salud.model.enums.EstadoPago;
import com.sisol.salud.model.enums.MetodoPago;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PagoResponse {

    private Long id;
    private Long citaId;
    private Long pacienteId;
    private BigDecimal monto;
    private MetodoPago metodoPago;
    private EstadoPago estadoPago;
    private String referenciaPago;
    private LocalDateTime fechaPago;
    private String notas;
    private LocalDateTime createdAt;
}
