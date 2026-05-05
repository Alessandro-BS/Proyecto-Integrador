package com.sisol.salud.controller.api;

import com.sisol.salud.dto.request.DisponibilidadRequest;
import com.sisol.salud.dto.response.DisponibilidadResponse;
import com.sisol.salud.dto.response.MedicoResponse;
import com.sisol.salud.service.DisponibilidadService;
import com.sisol.salud.service.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@RequiredArgsConstructor
@Tag(name = "Médicos", description = "Endpoints para la gestión de médicos y sus horarios")
public class MedicoRestController {

    private final MedicoService medicoService;
    private final DisponibilidadService disponibilidadService;

    // --- Endpoints de Médico ---

    @Operation(summary = "Listar médicos", description = "Obtiene la lista de todos los médicos")
    @GetMapping
    public ResponseEntity<List<MedicoResponse>> listarTodos() { // Solo ADMIN o PACIENTE
        return ResponseEntity.ok(medicoService.obtenerTodos());
    }

    @Operation(summary = "Filtrar por especialidad", description = "Obtiene médicos según el ID de su especialidad")
    @GetMapping("/especialidad/{especialidadId}")
    public ResponseEntity<List<MedicoResponse>> listarPorEspecialidad(@PathVariable Long especialidadId) { // Solo ADMIN
                                                                                                           // o PACIENTE
        return ResponseEntity.ok(medicoService.obtenerPorEspecialidad(especialidadId));
    }

    // --- Endpoints de Disponibilidad ---

    @Operation(summary = "Ver horarios de un médico", description = "Obtiene las disponibilidades de un médico específico")
    @GetMapping("/{medicoId}/disponibilidad")
    public ResponseEntity<List<DisponibilidadResponse>> verDisponibilidad(@PathVariable Long medicoId) { // Solo ADMIN o
                                                                                                         // PACIENTE
        return ResponseEntity.ok(disponibilidadService.obtenerPorMedico(medicoId));
    }

    @Operation(summary = "Agregar horario a médico", description = "Añade una nueva disponibilidad (Solo ADMIN o MEDICO)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @PostMapping("/{medicoId}/disponibilidad")
    public ResponseEntity<DisponibilidadResponse> agregarDisponibilidad( // Solo ADMIN o MEDICO
            @PathVariable Long medicoId,
            @Valid @RequestBody DisponibilidadRequest request) {
        return ResponseEntity.ok(disponibilidadService.agregarDisponibilidad(medicoId, request));
    }

    @Operation(summary = "Eliminar horario", description = "Elimina una disponibilidad (Solo ADMIN o MEDICO)")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEDICO')")
    @DeleteMapping("/disponibilidad/{id}")
    public ResponseEntity<Void> eliminarDisponibilidad(@PathVariable Long id) {
        disponibilidadService.eliminarDisponibilidad(id);
        return ResponseEntity.noContent().build();
    }
}
