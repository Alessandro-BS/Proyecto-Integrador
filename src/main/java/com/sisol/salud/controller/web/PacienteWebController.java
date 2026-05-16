package com.sisol.salud.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
        model.addAttribute("title", "Reservar Nueva Cita - Paso 1");
        return "paciente/reservar-cita";
    }

    @PostMapping("/reservar-cita")
    @PreAuthorize("hasRole('PACIENTE')")
    public String procesarPaso1(@org.springframework.web.bind.annotation.RequestParam("medico") Long medicoId, Model model) {
        // Redirige al paso 2 pasándole el ID del médico como parámetro en la URL
        return "redirect:/paciente/reservar-cita/paso2?medicoId=" + medicoId;
    }

    @GetMapping("/reservar-cita/paso2")
    @PreAuthorize("hasRole('PACIENTE')")
    public String reservarCitaPaso2(@org.springframework.web.bind.annotation.RequestParam("medicoId") Long medicoId, Model model) {
        model.addAttribute("medicoId", medicoId);
        model.addAttribute("title", "Reservar Nueva Cita - Paso 2");
        return "paciente/reservar-cita-paso2";
    }

    @org.springframework.web.bind.annotation.PostMapping("/reservar-cita/finalizar")
    @PreAuthorize("hasRole('PACIENTE')")
    public String finalizarReserva(
            @org.springframework.web.bind.annotation.RequestParam("medicoId") Long medicoId,
            @org.springframework.web.bind.annotation.RequestParam("fecha") String fecha,
            @org.springframework.web.bind.annotation.RequestParam("hora") String hora,
            @org.springframework.web.bind.annotation.RequestParam("motivoConsulta") String motivoConsulta,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        // Aquí llamaríamos a CitaService.reservarCita()
        // Por ahora simulamos éxito
        redirectAttributes.addFlashAttribute("mensajeExito", "¡Cita reservada exitosamente para el " + fecha + " a las " + hora + "!");
        return "redirect:/paciente/mis-citas";
    }
}
