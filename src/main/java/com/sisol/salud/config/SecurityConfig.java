package com.sisol.salud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.sisol.salud.security.jwt.JwtAuthEntryPoint;
import com.sisol.salud.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration // Indica que es una clase de configuración
@EnableWebSecurity // Habilita la seguridad web
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter; // Autenticación
    private final UserDetailsService userDetailsService; // Carga del usuario
    private final JwtAuthEntryPoint jwtAuthEntryPoint; // Manejo de errores de autenticación

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // Desactivamos CSRF porque usaremos JWT
                // Manejador de errores
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint))

                // "Sin estado"
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // Rutas públicas (sin tokens)
                        .requestMatchers(
                                "/",
                                "/index",
                                "/nosotros",
                                "/medicos",
                                "/servicios",
                                "/api/auth/**", // Endpoint de login y registro
                                "/css/**", // Recursos estáticos
                                "/js/**",
                                "/img/**",
                                "/webjars/**",
                                "/api-docs",
                                "/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error")
                        .permitAll()

                        // Rutas protegidas (necesitan estar autenticados)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/medico/**").hasRole("MEDICO")
                        .requestMatchers("/api/paciente/**").hasRole("PACIENTE")

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider())

                // Agregamos el filtro JWT antes del filtro de Spring Security
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
