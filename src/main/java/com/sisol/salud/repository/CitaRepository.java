package com.sisol.salud.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sisol.salud.model.entity.Cita;
import com.sisol.salud.model.enums.EstadoCita;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> { // Repositorio para citas.

    List<Cita> findByPacienteId(Long pacienteId); // Método para buscar citas por paciente.

    List<Cita> findByMedicoId(Long medicoId); // Método para buscar citas por médico.

    List<Cita> findByMedicoIdAndFecha(Long medicoId, LocalDate fecha); // Método para buscar citas por médico y fecha.

    List<Cita> findByEstado(EstadoCita estado); // Método para buscar citas por estado.
}
