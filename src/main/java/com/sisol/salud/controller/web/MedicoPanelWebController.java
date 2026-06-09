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
    public String citasDeHoy(
            @org.springframework.web.bind.annotation.RequestParam(value = "fecha", required = false) 
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fecha,
            java.security.Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                com.sisol.salud.model.entity.Medico medico = medicoRepository.findByUsuarioId(usuario.getId()).orElse(null);
                model.addAttribute("usuario", usuario);
                model.addAttribute("medico", medico);
                
                if (medico != null) {
                    java.time.LocalDate targetDate = (fecha != null) ? fecha : java.time.LocalDate.now();
                    
                    java.util.List<com.sisol.salud.model.entity.Cita> citasHoy = citaRepository.findByMedicoIdAndFecha(medico.getId(), targetDate)
                        .stream()
                        .sorted(java.util.Comparator.comparing(com.sisol.salud.model.entity.Cita::getHoraInicio))
                        .collect(java.util.stream.Collectors.toList());
                        
                    model.addAttribute("citasHoy", citasHoy);
                    model.addAttribute("fechaActual", targetDate);
                    model.addAttribute("fechaAnterior", targetDate.minusDays(1));
                    model.addAttribute("fechaSiguiente", targetDate.plusDays(1));
                }
            }
        }
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
    public String iniciarConsulta(@org.springframework.web.bind.annotation.RequestParam("citaId") Long citaId, java.security.Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                com.sisol.salud.model.entity.Medico medico = medicoRepository.findByUsuarioId(usuario.getId()).orElse(null);
                
                if (medico != null) {
                    com.sisol.salud.model.entity.Cita cita = citaRepository.findById(citaId).orElse(null);
                    // Validar que la cita pertenece a este médico
                    if (cita != null && cita.getMedico().getId().equals(medico.getId())) {
                        model.addAttribute("cita", cita);
                        model.addAttribute("paciente", cita.getPaciente());
                        model.addAttribute("usuarioPaciente", cita.getPaciente().getUsuario());
                    } else {
                        return "redirect:/panel-medico/citas-hoy";
                    }
                }
            }
        }
        
        model.addAttribute("title", "En Consulta");
        return "medico/consulta";
    }

    @org.springframework.web.bind.annotation.PostMapping("/consulta/finalizar")
    @PreAuthorize("hasRole('MEDICO')")
    public String finalizarConsulta(
            @org.springframework.web.bind.annotation.RequestParam("citaId") Long citaId,
            @org.springframework.web.bind.annotation.RequestParam("observaciones") String observaciones,
            java.security.Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
            
        if (principal != null) {
            String email = principal.getName();
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                com.sisol.salud.model.entity.Medico medico = medicoRepository.findByUsuarioId(usuario.getId()).orElse(null);
                
                if (medico != null) {
                    com.sisol.salud.model.entity.Cita cita = citaRepository.findById(citaId).orElse(null);
                    // Validar que la cita pertenece a este médico
                    if (cita != null && cita.getMedico().getId().equals(medico.getId())) {
                        cita.setEstado(com.sisol.salud.model.enums.EstadoCita.COMPLETADA);
                        cita.setObservaciones(observaciones);
                        citaRepository.save(cita);
                        
                        redirectAttributes.addFlashAttribute("mensajeExito", "Consulta finalizada exitosamente. El diagnóstico ha sido guardado.");
                    }
                }
            }
        }
        
        return "redirect:/panel-medico/citas-hoy";
    }
}
