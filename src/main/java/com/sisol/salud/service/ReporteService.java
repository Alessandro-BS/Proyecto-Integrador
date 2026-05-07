package com.sisol.salud.service;

import com.sisol.salud.dto.response.ReporteDashboardResponse;
import com.sisol.salud.model.enums.EstadoCita;
import com.sisol.salud.repository.CitaRepository;
import com.sisol.salud.repository.MedicoRepository;
import com.sisol.salud.repository.PacienteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final CitaRepository citaRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;

    public ReporteDashboardResponse generarDashboard() {
        // Contadores básicos
        long totalPacientes = pacienteRepository.count();
        long totalMedicos = medicoRepository.count();
        
        // Contadores de citas por estado
        long citasCompletadas = citaRepository.countByEstado(EstadoCita.COMPLETADA);
        long citasPendientes = citaRepository.countByEstado(EstadoCita.PENDIENTE);
        long citasNoAsistidas = citaRepository.countByEstado(EstadoCita.NO_ASISTIO);

        // Calcular Tasa de Ausentismo (No Asistió / Total de Citas Finalizadas)
        double tasaAusentismo = 0.0;
        long totalCitasFinalizadas = citasCompletadas + citasNoAsistidas;
        
        if (totalCitasFinalizadas > 0) {
            tasaAusentismo = ((double) citasNoAsistidas / totalCitasFinalizadas) * 100.0;
        }

        // Redondeo a 2 decimales
        tasaAusentismo = Math.round(tasaAusentismo * 100.0) / 100.0;

        // Construir y retornar el DTO final usando patrón Builder
        return ReporteDashboardResponse.builder()
                .totalPacientes(totalPacientes)
                .totalMedicos(totalMedicos)
                .totalCitasCompletadas(citasCompletadas)
                .totalCitasPendientes(citasPendientes)
                .tasaAusentismoPorcentaje(tasaAusentismo)
                .topEspecialidades(citaRepository.findTopEspecialidades())
                .citasPorDia(citaRepository.countCitasPorDia())
                .build();
    }
}
