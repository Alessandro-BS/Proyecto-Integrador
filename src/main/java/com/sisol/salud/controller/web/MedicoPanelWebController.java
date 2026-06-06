package com.sisol.salud.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/panel-medico")
@RequiredArgsConstructor
public class MedicoPanelWebController {

    private final com.sisol.salud.repository.UsuarioRepository usuarioRepository;
    private final com.sisol.salud.repository.MedicoRepository medicoRepository;
    private final com.sisol.salud.repository.CitaRepository citaRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('MEDICO')")
    public String dashboard(java.security.Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                com.sisol.salud.model.entity.Medico medico = medicoRepository.findByUsuarioId(usuario.getId()).orElse(null);
                model.addAttribute("usuario", usuario);
                model.addAttribute("medico", medico);
                
                if (medico != null) {
                    java.time.LocalDate hoy = java.time.LocalDate.now();
                    java.util.List<com.sisol.salud.model.entity.Cita> citasHoy = citaRepository.findByMedicoIdAndFecha(medico.getId(), hoy);
                    
                    // Solo PENDIENTE o CONFIRMADA cuentan para la agenda activa de hoy
                    long countCitasHoy = citasHoy.stream()
                        .filter(c -> c.getEstado() == com.sisol.salud.model.enums.EstadoCita.PENDIENTE || 
                                     c.getEstado() == com.sisol.salud.model.enums.EstadoCita.CONFIRMADA)
                        .count();
                        
                    com.sisol.salud.model.entity.Cita proximaCita = citasHoy.stream()
                        .filter(c -> (c.getEstado() == com.sisol.salud.model.enums.EstadoCita.PENDIENTE || 
                                      c.getEstado() == com.sisol.salud.model.enums.EstadoCita.CONFIRMADA) && 
                                      c.getHoraInicio().isAfter(java.time.LocalTime.now().minusMinutes(30))) // Damos 30 min de gracia
                        .min(java.util.Comparator.comparing(com.sisol.salud.model.entity.Cita::getHoraInicio))
                        .orElse(null);
                        
                    model.addAttribute("citasHoyCount", countCitasHoy);
                    model.addAttribute("proximaCita", proximaCita);
                }
            }
        }
        
        model.addAttribute("title", "Mi Panel - Médico");
        return "medico/dashboard";
    }

    @GetMapping("/citas-hoy")
    @PreAuthorize("hasRole('MEDICO')")
    public String citasDeHoy(Model model) {
        model.addAttribute("title", "Mis Citas de Hoy");
        return "medico/citas-hoy";
    }

    @GetMapping("/disponibilidad")
    @PreAuthorize("hasRole('MEDICO')")
    public String gestionarDisponibilidad(Model model) {
        model.addAttribute("title", "Mi Disponibilidad");
        return "medico/disponibilidad";
    }

    @GetMapping("/consulta")
    @PreAuthorize("hasRole('MEDICO')")
    public String iniciarConsulta(Model model) {
        model.addAttribute("title", "En Consulta");
        return "medico/consulta";
    }
}
