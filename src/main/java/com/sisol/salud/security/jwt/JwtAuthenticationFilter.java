package com.sisol.salud.security.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sisol.salud.security.CustomUserDetailsService;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component // Componente Bean para Spring.
@RequiredArgsConstructor // Genera constructor con parámetros requeridos.
public class JwtAuthenticationFilter extends OncePerRequestFilter { // Filtro para autenticación JWT.
    // Ejecuciones por cada petición HTTP.
    private final JwtService jwtService; // Inyección de dependencia del servicio JWT.
    private final CustomUserDetailsService userDetailsService; // Inyección de dependencia del servicio de usuarios.

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1. Verificar si el token existe y empieza con "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extraer el token (quitando "Bearer ")
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);

        // 3. Si tenemos el email y el usuario aún no está autenticado en el contexto
        // actual
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Cargamos los datos del usuario desde la DB
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 4. Validar que el token sea válido
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 5. Creamos el objeto de autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Asignamos la autenticación al contexto de Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 7. Se continua con el resto de filtros
        filterChain.doFilter(request, response);
    }
}
