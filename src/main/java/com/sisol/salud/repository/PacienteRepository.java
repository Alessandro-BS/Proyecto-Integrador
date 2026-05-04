package com.sisol.salud.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sisol.salud.model.entity.Paciente;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> { // Repositorio para pacientes.

    Optional<Paciente> findByUsuarioId(Long usuarioId); // Método para buscar un paciente por usuario.
}
