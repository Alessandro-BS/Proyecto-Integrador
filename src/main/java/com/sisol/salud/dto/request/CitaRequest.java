package com.sisol.salud.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CitaRequest {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long pacienteId;

    @NotNull(message = "El ID del médico es obligatorio")
    private Long medicoId;

    @NotNull(message = "El ID de la especialidad es obligatorio")
    private Long especialidadId;

    @NotNull(message = "La fecha y hora de la cita es obligatoria")
    @Future(message = "La cita debe programarse para una fecha futura")
    private LocalDateTime fechaHora;

    // Motivo de consulta opcional
    private String motivoConsulta;

    // Campos de Pago
    @NotNull(message = "El método de pago es obligatorio")
    private com.sisol.salud.model.enums.MetodoPago metodoPago;

    private String referenciaPago;
}
