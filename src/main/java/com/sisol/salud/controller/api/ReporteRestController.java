package com.sisol.salud.controller.api;

import com.sisol.salud.dto.response.ReporteDashboardResponse;
import com.sisol.salud.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/reportes")
@RequiredArgsConstructor
public class ReporteRestController {

    private final ReporteService reporteService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReporteDashboardResponse> obtenerDatosDashboard() {
        ReporteDashboardResponse reporte = reporteService.generarDashboard();
        return ResponseEntity.ok(reporte);
    }
}
