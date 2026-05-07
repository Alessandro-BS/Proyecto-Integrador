package com.sisol.salud.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDashboardResponse {
    
    // Estadísticas generales (tarjetas superiores del dashboard)
    private long totalPacientes;
    private long totalMedicos;
    private long totalCitasCompletadas;
    private long totalCitasPendientes;
    
    // Tasas y porcentajes
    private double tasaAusentismoPorcentaje; // % de citas NO_ASISTIO
    
    // Listas para gráficos o tablas top
    private List<TopEspecialidadResponse> topEspecialidades;
    private List<CitasPorDiaResponse> citasPorDia; // Para gráfico de línea o barras
}
