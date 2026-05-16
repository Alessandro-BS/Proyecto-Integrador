package com.sisol.salud.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.sisol.salud.service.CitaService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/paciente")
@RequiredArgsConstructor
public class PacienteWebController {

    private final CitaService citaService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PACIENTE')")
    public String dashboard(Model model) {
        // TODO: En el futuro obtendremos el ID del paciente logueado usando SecurityContextHolder
        // Por ahora lo pasaremos como prueba o lo dejaremos vacío
        model.addAttribute("title", "Mi Panel - Paciente");
        return "paciente/dashboard";
    }

    @GetMapping("/mis-citas")
    @PreAuthorize("hasRole('PACIENTE')")
    public String misCitas(Model model) {
        model.addAttribute("title", "Mis Citas");
        return "paciente/mis-citas";
    }

    @GetMapping("/reservar-cita")
    @PreAuthorize("hasRole('PACIENTE')")
    public String reservarCita(Model model) {
        model.addAttribute("title", "Reservar Nueva Cita");
        return "paciente/reservar-cita";
    }
}
