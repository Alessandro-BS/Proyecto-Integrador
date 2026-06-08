package com.sisol.salud.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sisol.salud.model.entity.Especialidad;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> { // Repositorio para especialidades

    Optional<Especialidad> findByNombre(String nombre); // Método para buscar una especialidad por nombre

    java.util.List<Especialidad> findByActivoTrue(); // Método para buscar especialidades activas

    org.springframework.data.domain.Page<Especialidad> findByActivoTrue(org.springframework.data.domain.Pageable pageable); // Método para buscar especialidades activas paginadas
}
