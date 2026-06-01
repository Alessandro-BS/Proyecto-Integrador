package com.sisol.salud.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sisol.salud.model.entity.Pago;
import com.sisol.salud.model.enums.EstadoPago;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByCitaId(Long citaId);

    List<Pago> findByPacienteId(Long pacienteId);

    List<Pago> findByEstadoPago(EstadoPago estadoPago);
}
