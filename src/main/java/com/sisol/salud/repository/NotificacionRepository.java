package com.sisol.salud.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sisol.salud.model.entity.Notificacion;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> { // Repositorio para notificaciones.

    List<Notificacion> findByUsuarioId(Long usuarioId); // Método para buscar notificaciones por usuario.

    List<Notificacion> findByEnviadoFalse(); // Método para buscar notificaciones no enviadas.
}
