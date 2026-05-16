package com.sisol.salud.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sisol.salud.model.entity.Cita;
import com.sisol.salud.model.enums.EstadoCita;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> { // Repositorio para citas.

    List<Cita> findByPacienteId(Long pacienteId); // Método para buscar citas por paciente.

    List<Cita> findByMedicoId(Long medicoId); // Método para buscar citas por médico.

    List<Cita> findByMedicoIdAndFecha(Long medicoId, LocalDate fecha); // Método para buscar citas por médico y fecha.

    List<Cita> findByEstado(EstadoCita estado); // Método para buscar citas por estado.

    List<Cita> findByFechaAndEstado(LocalDate fecha, EstadoCita estado);

    @Query("SELECT c FROM Cita c WHERE c.medico.id = :medicoId AND c.fecha = :fecha AND c.estado != 'CANCELADA'")
    List<Cita> buscarCitasPorMedicoYDia(@Param("medicoId") Long medicoId, @Param("fecha") LocalDate fecha);

    // ==========================================
    // QUERIES PARA REPORTES Y DASHBOARD (FASE 5)
    // ==========================================

    long countByEstado(EstadoCita estado);

    @Query("SELECT new com.sisol.salud.dto.response.TopEspecialidadResponse(c.medico.especialidad.nombre, COUNT(c)) " +
           "FROM Cita c GROUP BY c.medico.especialidad.nombre ORDER BY COUNT(c) DESC")
    List<com.sisol.salud.dto.response.TopEspecialidadResponse> findTopEspecialidades();

    @Query("SELECT new com.sisol.salud.dto.response.CitasPorDiaResponse(c.fecha, COUNT(c)) " +
           "FROM Cita c GROUP BY c.fecha ORDER BY c.fecha ASC")
    List<com.sisol.salud.dto.response.CitasPorDiaResponse> countCitasPorDia();
}
