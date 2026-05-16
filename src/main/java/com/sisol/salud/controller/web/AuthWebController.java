package com.sisol.salud.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthWebController {

    private final com.sisol.salud.service.AuthService authService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("title", "Iniciar Sesión");
        return "auth/login";
    }

    @org.springframework.web.bind.annotation.PostMapping("/login")
    public String procesarLogin(
            @org.springframework.web.bind.annotation.ModelAttribute com.sisol.salud.dto.request.LoginRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            com.sisol.salud.dto.response.AuthResponse authResponse = authService.login(request);
            crearCookieJwt(authResponse.getToken(), response);
            return redirigirSegunRol(authResponse.getRol());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Credenciales inválidas. Por favor intente nuevamente.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/registro")
    public String registroPage(Model model) {
        model.addAttribute("title", "Crear Cuenta");
        return "auth/registro";
    }

    @org.springframework.web.bind.annotation.PostMapping("/registro")
    public String procesarRegistro(
            @org.springframework.web.bind.annotation.ModelAttribute com.sisol.salud.dto.request.RegistroRequest request,
            jakarta.servlet.http.HttpServletResponse response,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            com.sisol.salud.dto.response.AuthResponse authResponse = authService.register(request);
            crearCookieJwt(authResponse.getToken(), response);
            redirectAttributes.addFlashAttribute("mensajeExito", "¡Cuenta creada exitosamente! Bienvenido a SISOL Salud.");
            return "redirect:/paciente/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar: " + e.getMessage());
            return "redirect:/auth/registro";
        }
    }
    
    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpServletResponse response) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("JWT-TOKEN", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // Eliminar cookie
        response.addCookie(cookie);
        return "redirect:/";
    }

    private void crearCookieJwt(String token, jakarta.servlet.http.HttpServletResponse response) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("JWT-TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24); // 1 día
        response.addCookie(cookie);
    }

    private String redirigirSegunRol(String rol) {
        return switch (rol) {
            case "ADMIN" -> "redirect:/admin/dashboard";
            case "MEDICO" -> "redirect:/panel-medico/dashboard";
            default -> "redirect:/paciente/dashboard";
        };
    }
}
