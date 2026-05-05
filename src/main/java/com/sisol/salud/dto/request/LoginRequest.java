package com.sisol.salud.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Lombok crea getters/setters, toString, equals, hashCode
@Builder
@AllArgsConstructor // Constructor con todos los parámetros
@NoArgsConstructor // Constructor sin parámetros
public class LoginRequest {

    @NotBlank(message = "El email no puede estar vacío") // Validación para que no sea nulo o espacios en blanco
    @Email(message = "Debe tener un formato de email válido") // Validación de formato email
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
}
