package com.sisol.salud.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.sisol.salud.dto.request.LoginRequest;
import com.sisol.salud.dto.response.AuthResponse;
import com.sisol.salud.model.enums.Rol;
import com.sisol.salud.model.entity.Usuario;
import com.sisol.salud.repository.PacienteRepository;
import com.sisol.salud.repository.UsuarioRepository;
import com.sisol.salud.security.jwt.JwtService;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioPrueba;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        usuarioPrueba = new Usuario();
        usuarioPrueba.setId(1L);
        usuarioPrueba.setEmail("test@sisol.com");
        usuarioPrueba.setPassword("password123");
        usuarioPrueba.setDni("12345678");
        usuarioPrueba.setRol(Rol.PACIENTE);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@sisol.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void login_ConCredencialesValidas_DebeRetornarAuthResponse() {
        // Arrange
        UserDetails userDetailsMock = User.builder()
            .username("test@sisol.com")
            .password("password123")
            .roles("PACIENTE")
            .build();
            
        when(usuarioRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(usuarioPrueba));
        when(userDetailsService.loadUserByUsername(loginRequest.getEmail())).thenReturn(userDetailsMock);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("token-jwt-simulado");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("token-jwt-simulado", response.getToken());
        assertEquals("test@sisol.com", response.getEmail());
        assertEquals("PACIENTE", response.getRol());

        // Verificar que el authenticationManager.authenticate fue llamado correctamente
        verify(authenticationManager).authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
    }
}
