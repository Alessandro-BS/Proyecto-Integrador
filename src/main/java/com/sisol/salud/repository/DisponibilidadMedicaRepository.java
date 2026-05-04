package com.sisol.salud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sisol.salud.model.entity.DisponibilidadMedica;
import com.sisol.salud.model.enums.DiaSemana;

@Repository
public interface DisponibilidadMedicaRepository extends JpaRepository<DisponibilidadMedica, Long> {
    // Repositorio para disponibilidad médica

    List<DisponibilidadMedica> findByMedicoId(Long medicoId);
    // Método para buscar disponibilidad médica por médico.

    List<DisponibilidadMedica> findByMedicoIdAndDiaSemanaAndActivoTrue(Long medicoId, DiaSemana diaSemana);
    // Método para buscar disponibilidad médica por médico y día de la semana.
}
