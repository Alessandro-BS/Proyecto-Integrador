package com.sisol.salud.controller.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sisol.salud.dto.request.LoginRequest;
import com.sisol.salud.dto.response.AuthResponse;
import com.sisol.salud.service.AuthService;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc; // Cliente HTTP simulado

    @MockBean
    private AuthService authService; // Simulamos la capa de servicio (no tocamos BD real)

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos a JSON

    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@sisol.com", "password123");
        
        authResponse = new AuthResponse();
        authResponse.setToken("fake-jwt-token");
        authResponse.setEmail("test@sisol.com");
        authResponse.setRol("PACIENTE");
    }

    @Test
    void login_ConCredencialesValidas_DebeRetornar200YToken() throws Exception {
        // Arrange: Le decimos a nuestro mock que cuando reciba cualquier LoginRequest, devuelva la respuesta falsa
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // Act & Assert: Simulamos una petición POST a /api/auth/login
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))) // Convertimos el DTO a JSON
                
                // Validamos que la respuesta HTTP sea 200 OK
                .andExpect(status().isOk())
                
                // Validamos que el JSON de respuesta contenga el token
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.email").value("test@sisol.com"))
                .andExpect(jsonPath("$.rol").value("PACIENTE"));
    }

    @Test
    void login_ConCredencialesVacias_DebeRetornar400BadRequest() throws Exception {
        // Arrange: Un request inválido (sin email ni password)
        LoginRequest requestInvalido = new LoginRequest("", "");

        // Act & Assert: Debe fallar en la validación @Valid del controlador
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestInvalido)))
                
                // Esperamos un error 400 por no pasar la validación
                .andExpect(status().isBadRequest());
    }
}
