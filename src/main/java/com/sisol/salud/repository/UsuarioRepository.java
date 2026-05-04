package com.sisol.salud.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sisol.salud.model.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> { // Repositorio para usuarios.

    Optional<Usuario> findByDni(String dni); // Método para buscar un usuario por DNI.

    Optional<Usuario> findByEmail(String email); // Método para buscar un usuario por email.

    boolean existsByDni(String dni); // Método para verificar si existe un usuario por DNI.

    boolean existsByEmail(String email); // Método para verificar si existe un usuario por email.
}
