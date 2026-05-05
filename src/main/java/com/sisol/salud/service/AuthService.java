package com.sisol.salud.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sisol.salud.dto.request.LoginRequest;
import com.sisol.salud.dto.request.RegistroRequest;
import com.sisol.salud.dto.response.AuthResponse;
import com.sisol.salud.model.entity.Paciente;
import com.sisol.salud.model.entity.Usuario;
import com.sisol.salud.model.enums.Rol;
import com.sisol.salud.repository.PacienteRepository;
import com.sisol.salud.repository.UsuarioRepository;
import com.sisol.salud.security.jwt.JwtService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public AuthResponse login(LoginRequest request) {
        // 1. Spring Security maneja la verificación de contraseña internamente
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        // 2. Si pasa, buscamos al usuario
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());

        // 3. Generar Token
        String jwtToken = jwtService.generateToken(userDetails);

        // 4. Devolvemos la respuesta
        return AuthResponse.builder()
                .token(jwtToken)
                .email(usuario.getEmail())
                .nombre(usuario.getNombre() + " " + usuario.getApellido())
                .rol(usuario.getRol().name())
                .build();
    }

    @Transactional
    public AuthResponse register(RegistroRequest request) {
        // 1. Valdar que no exista el email ni el DNI
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        if (usuarioRepository.existsByDni(request.getDni())) {
            throw new RuntimeException("El DNI ya está registrado");
        }

        // 2. Crear y guardar el usuario
        Usuario usuario = Usuario.builder()
                .dni(request.getDni())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Encriptar contraseña
                .telefono(request.getTelefono())
                .rol(Rol.PACIENTE) // Todo el que se registra por la web es paciente
                .activo(true)
                .build();

        Usuario savedUsuario = usuarioRepository.save(usuario);

        // 3. Crear y guardar el registro de Paciente asociado al usuario
        Paciente paciente = Paciente.builder()
                .usuario(savedUsuario)
                // Fecha de nacimiento, direccion, etc.
                // Irán nulos por el momento o añadirse luego al RegistroRequest
                .build();

        pacienteRepository.save(paciente);

        // 4. Generar el Token JWT para logueo directo
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String jwtToken = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(jwtToken)
                .email(usuario.getEmail())
                .nombre(usuario.getNombre() + " " + usuario.getApellido())
                .rol(usuario.getRol().name())
                .build();
    }
}
