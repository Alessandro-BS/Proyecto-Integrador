package com.sisol.salud.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pacientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID Autoincremental
    private Long id;

    // Relacion 1 a 1 con Usuario (Un paciente es un usuario)
    // FetchType.LAZY: Solo cargará datos del usuario cuando se acceda a él
    // JoinColumn: Nombre de la columna en la tabla pacientes que hace referencia al
    // usuario
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento; // Fecha de Nacimiento

    @Column(length = 255)
    private String direccion; // Dirección

    @Column(name = "grupo_sanguineo", length = 5)
    private String grupoSanguineo; // Grupo Sanguineo

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Fecha de Creación

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Fecha de Actualización
}
