package com.sisol.salud.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sisol.salud.dto.request.LoginRequest;
import com.sisol.salud.dto.request.RegistroRequest;
import com.sisol.salud.dto.response.AuthResponse;
import com.sisol.salud.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController // Indica que es un controlador REST
@RequestMapping("/api/auth") // Prefijo para todas las peticiones de este controlador
@RequiredArgsConstructor // Crea el constructor con los campos finales
@Tag(name = "Autenticación", description = "Endpoints para Login y Registro de usuarios")
public class AuthRestController {

    private final AuthService authService; // Inyección de dependencias

    // Endpoint para iniciar sesión
    // Operation es una anotación de Swagger
    @Operation(summary = "Iniciar sesión", description = "Valida credenciales y devuelve un token JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Registrar nuevo paciente", description = "Crea un usuario y un perfil de paciente, devuelve JWT")
    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
}
