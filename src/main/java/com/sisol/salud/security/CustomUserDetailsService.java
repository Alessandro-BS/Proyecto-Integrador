package com.sisol.salud.security;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sisol.salud.model.entity.Usuario;
import com.sisol.salud.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service // Indica un servicio
@RequiredArgsConstructor // Genera constructor con parámetros requeridos.
public class CustomUserDetailsService implements UserDetailsService { // Interfaz para cargar usuarios

    private final UsuarioRepository usuarioRepository; // Inyección de dependencia del repositorio.

    @Override // Override: Sobreescribe el método de la interfaz UserDetailsService.
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException { // Buscar usuario por email.
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("El usuario está inactivo: " + email);
        }

        // Spring Security requiere una lista de Authorities (Roles).
        // "ROLE_" es un estándar en Spring.
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name());

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPassword(),
                Collections.singletonList(authority));
    }
}
