package com.sisol.salud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sisol.salud.model.entity.ArchivoCita;

import java.util.List;

@Repository
public interface ArchivoCitaRepository extends JpaRepository<ArchivoCita, Long> {
    List<ArchivoCita> findByCitaId(Long citaId);
}
