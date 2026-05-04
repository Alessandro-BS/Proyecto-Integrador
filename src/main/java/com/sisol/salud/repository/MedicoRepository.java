package com.sisol.salud.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sisol.salud.model.entity.Medico;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> { // Repositorio para médicos

    Optional<Medico> findByUsuarioId(Long usuarioId); // Método para buscar un médico por usuario.

    List<Medico> findByEspecialidadId(Long especialidadId); // Método para buscar médicos por especialidad.
}
