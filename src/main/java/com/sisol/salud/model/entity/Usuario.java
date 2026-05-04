package com.sisol.salud.model.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.sisol.salud.model.enums.Rol;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity // Entidad de Persistencia
@Table(name = "usuarios") // Tabla usuarios en la DB
@Getter // Getters
@Setter // Setters
@NoArgsConstructor // Constructor sin argumentos
@AllArgsConstructor // Constructor con parámetros
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincrementable
    private Long id;

    @Column(nullable = false, unique = true, length = 15) // DNI
    private String dni;

    @Column(nullable = false, length = 100) // Nombres
    private String nombre;

    @Column(nullable = false, length = 100) // Apellidos
    private String apellido;

    @Column(nullable = false, unique = true, length = 150) // Correo
    private String email;

    @Column(nullable = false) // Password
    private String password;

    @Column(length = 20)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.PACIENTE; // Por defecto siempre será paciente

    @Column(nullable = false)
    private boolean activo = true; // Por defecto siempre será activo

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Fecha de creación

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Fecha de actualización

}
