package com.sisol.salud.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sisol.salud.dto.request.CitaRequest;
import com.sisol.salud.dto.response.CitaResponse;
import com.sisol.salud.service.CitaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
@Tag(name = "Citas", description = "Gestión del ciclo de vida de las citas médicas")
public class CitaRestController {

    private final CitaService citaService;

    // 1. Agendar Cita (Disponible para ADMIN y PACIENTE)
    @Operation(
        summary = "Reservar una nueva cita",
        description = "Permite agendar una cita verificando disponibilidad y reglas de negocio.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Cita creada con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o falta de disponibilidad")
        }
    )
    @PostMapping("/reservar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PACIENTE')")
    public ResponseEntity<CitaResponse> reservarCita(@jakarta.validation.Valid @RequestBody CitaRequest request) {
        CitaResponse response = citaService.reservarCita(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Cancelar Cita (Disponible para ADMIN y PACIENTE)
    @Operation(summary = "Cancelar una cita", description = "Cancela una cita si tiene más de 2 horas de anticipación.")
    @PostMapping("/{citaId}/cancelar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PACIENTE')")
    public ResponseEntity<Void> cancelarCita(
            @PathVariable Long citaId,
            @RequestParam Long usuarioId,
            @RequestParam boolean esAdmin) {

        // TODO: En el futuro, el usuarioId y esAdmin se extraerán del token JWT
        // de la sesión actual, pero por ahora los pedimos como parámetros para pruebas.
        citaService.cancelarCita(citaId, usuarioId, esAdmin);
        return ResponseEntity.noContent().build();
    }

    // 3. Completar Cita (Solo para MÉDICOS)
    @Operation(summary = "Completar cita", description = "El médico marca la cita como realizada y añade observaciones.")
    @PostMapping("/{citaId}/completar")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<Void> completarCita(
            @PathVariable Long citaId,
            @RequestParam Long medicoId,
            @RequestBody String observaciones) {

        citaService.completarCita(citaId, medicoId, observaciones);
        return ResponseEntity.ok().build();
    }

    // 4. Marcar como inasistencia (Solo para MÉDICOS)
    @Operation(summary = "Marcar inasistencia", description = "El médico marca que el paciente no asistió a la cita.")
    @PostMapping("/{citaId}/no-asistio")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<Void> marcarComoNoAsistio(
            @PathVariable Long citaId,
            @RequestParam Long medicoId) {

        citaService.marcarComoNoAsistio(citaId, medicoId);
        return ResponseEntity.ok().build();
    }
}
