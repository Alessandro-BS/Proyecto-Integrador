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
    private final com.sisol.salud.repository.UsuarioRepository usuarioRepository;
    private final com.sisol.salud.repository.PacienteRepository pacienteRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('PACIENTE')")
    public String dashboard(java.security.Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                com.sisol.salud.model.entity.Paciente paciente = pacienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
                model.addAttribute("usuario", usuario);
                model.addAttribute("paciente", paciente);
            }
        }
        
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
    public String reservarCitaPaso1(Model model) {
        model.addAttribute("title", "Selecciona una Especialidad");
        return "paciente/reservar-paso1";
    }

    @PostMapping("/reservar-cita/paso2")
    @PreAuthorize("hasRole('PACIENTE')")
    public String procesarPaso1(
            @org.springframework.web.bind.annotation.RequestParam("especialidad") String especialidad,
            Model model) {
        model.addAttribute("especialidad", especialidad);
        model.addAttribute("title", "Selecciona un Especialista");
        return "paciente/reservar-paso2";
    }

    @PostMapping("/reservar-cita/paso3")
    @PreAuthorize("hasRole('PACIENTE')")
    public String procesarPaso2(
            @org.springframework.web.bind.annotation.RequestParam("especialidad") String especialidad,
            @org.springframework.web.bind.annotation.RequestParam("medico") String medico,
            Model model) {
        model.addAttribute("especialidad", especialidad);
        model.addAttribute("medico", medico);
        model.addAttribute("title", "Selecciona Fecha y Horario");
        return "paciente/reservar-paso3";
    }

    @PostMapping("/reservar-cita/finalizar")
    @PreAuthorize("hasRole('PACIENTE')")
    public String finalizarReserva(
            @org.springframework.web.bind.annotation.RequestParam("especialidad") String especialidad,
            @org.springframework.web.bind.annotation.RequestParam("medico") String medico,
            @org.springframework.web.bind.annotation.RequestParam("fecha") String fecha,
            @org.springframework.web.bind.annotation.RequestParam("hora") String hora,
            @org.springframework.web.bind.annotation.RequestParam("metodoPago") String metodoPago,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        // Simulación de guardado de cita
        redirectAttributes.addFlashAttribute("mensajeExito", 
            "¡Cita reservada exitosamente para el " + fecha + " a las " + hora + " con el doctor " + medico + "!");
        return "redirect:/paciente/mis-citas";
    }
}
