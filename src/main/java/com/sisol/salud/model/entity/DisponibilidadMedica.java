package com.sisol.salud.model.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.sisol.salud.model.enums.DiaSemana;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "disponibilidad_medica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisponibilidadMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacion Muchos a 1 con Médico: Un médico puede tener muchas disponibilidades
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false)
    private DiaSemana diaSemana; // Dia de la semana

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio; // Hora de inicio

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin; // Hora final

    @Column(name = "duracion_consulta_min", nullable = false)
    private Integer duracionConsultaMin = 30; // 30 minutos por defecto

    @Column(nullable = false)
    private boolean activo = true; // Activo por defecto

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Fecha de Creación

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Fecha de Actualización

}
