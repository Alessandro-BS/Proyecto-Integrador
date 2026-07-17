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
    private final com.sisol.salud.repository.DisponibilidadMedicaRepository disponibilidadRepository;
    private final com.sisol.salud.repository.ArchivoCitaRepository archivoCitaRepository;

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
                        
                    java.time.LocalTime horaFinAgenda = citasHoy.stream()
                        .filter(c -> c.getEstado() == com.sisol.salud.model.enums.EstadoCita.PENDIENTE || 
                                     c.getEstado() == com.sisol.salud.model.enums.EstadoCita.CONFIRMADA)
                        .map(com.sisol.salud.model.entity.Cita::getHoraFin)
                        .max(java.time.LocalTime::compareTo)
                        .orElse(null);
                        
                    long pacientesAtendidosMes = citaRepository.findByMedicoId(medico.getId()).stream()
                        .filter(c -> c.getFecha().getMonth() == hoy.getMonth() && c.getFecha().getYear() == hoy.getYear())
                        .filter(c -> c.getEstado() == com.sisol.salud.model.enums.EstadoCita.COMPLETADA)
                        .count();
                        
                    long citasCompletadasHoy = citasHoy.stream()
                        .filter(c -> c.getEstado() == com.sisol.salud.model.enums.EstadoCita.COMPLETADA)
                        .count();

                    model.addAttribute("citasHoyCount", countCitasHoy);
                    model.addAttribute("proximaCita", proximaCita);
                    model.addAttribute("horaFinAgenda", horaFinAgenda);
                    model.addAttribute("pacientesAtendidosMes", pacientesAtendidosMes);
                    model.addAttribute("citasCompletadasHoy", citasCompletadasHoy);
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
    public String gestionarDisponibilidad(java.security.Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                com.sisol.salud.model.entity.Medico medico = medicoRepository.findByUsuarioId(usuario.getId()).orElse(null);
                model.addAttribute("usuario", usuario);
                model.addAttribute("medico", medico);
                
                if (medico != null) {
                    java.time.LocalDate hoy = java.time.LocalDate.now();
                    java.time.LocalDate inicioMes = hoy.withDayOfMonth(1);
                    java.time.LocalDate finMes = hoy.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

                    java.util.List<com.sisol.salud.model.entity.Cita> todasCitas = citaRepository.findByMedicoId(medico.getId());
                    
                    long pacientesAtendidosMes = todasCitas.stream()
                        .filter(c -> c.getEstado() == com.sisol.salud.model.enums.EstadoCita.COMPLETADA)
                        .filter(c -> !c.getFecha().isBefore(inicioMes) && !c.getFecha().isAfter(finMes))
                        .map(c -> c.getPaciente().getId())
                        .distinct()
                        .count();
                        
                    long citasPendientes = todasCitas.stream()
                        .filter(c -> c.getEstado() == com.sisol.salud.model.enums.EstadoCita.PENDIENTE || c.getEstado() == com.sisol.salud.model.enums.EstadoCita.CONFIRMADA)
                        .count();
                        
                    model.addAttribute("pacientesAtendidosMes", pacientesAtendidosMes);
                    model.addAttribute("citasPendientes", citasPendientes);
                    
                    java.util.List<com.sisol.salud.model.entity.DisponibilidadMedica> disponibilidades = disponibilidadRepository.findByMedicoId(medico.getId());
                    model.addAttribute("disponibilidades", disponibilidades);
                }
            }
        }
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
            @org.springframework.web.bind.annotation.RequestParam(value = "archivos", required = false) org.springframework.web.multipart.MultipartFile[] archivos,
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
                        
                        // Guardar archivos adjuntos si existen
                        if (archivos != null && archivos.length > 0) {
                            for (org.springframework.web.multipart.MultipartFile file : archivos) {
                                if (!file.isEmpty()) {
                                    try {
                                        String contentType = file.getContentType();
                                        byte[] bytes = file.getBytes();
                                        String base64Content = "data:" + contentType + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
                                        
                                        com.sisol.salud.model.entity.ArchivoCita archivoCita = com.sisol.salud.model.entity.ArchivoCita.builder()
                                                .cita(cita)
                                                .nombreArchivo(file.getOriginalFilename())
                                                .tipoContenido(contentType)
                                                .datosBase64(base64Content)
                                                .build();
                                                
                                        archivoCitaRepository.save(archivoCita);
                                    } catch (Exception e) {
                                        // Ignore upload error per file or handle it
                                        System.out.println("Error procesando archivo: " + e.getMessage());
                                    }
                                }
                            }
                        }
                        
                        redirectAttributes.addFlashAttribute("mensajeExito", "Consulta finalizada exitosamente. El diagnóstico ha sido guardado.");
                    }
                }
            }
        }
        
        return "redirect:/panel-medico/citas-hoy";
    }

    @org.springframework.web.bind.annotation.GetMapping("/api/citas")
    @org.springframework.web.bind.annotation.ResponseBody
    @PreAuthorize("hasRole('MEDICO')")
    public java.util.List<com.sisol.salud.dto.response.CalendarEventDto> getCitasParaCalendario(java.security.Principal principal) {
        if (principal == null) return java.util.Collections.emptyList();
        
        String email = principal.getName();
        com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) return java.util.Collections.emptyList();
        
        com.sisol.salud.model.entity.Medico medico = medicoRepository.findByUsuarioId(usuario.getId()).orElse(null);
        if (medico == null) return java.util.Collections.emptyList();
        
        return citaRepository.findByMedicoId(medico.getId()).stream().map(cita -> {
            String color = switch (cita.getEstado().name()) {
                case "CONFIRMADA" -> "#198754"; // Success green
                case "PENDIENTE" -> "#ffc107"; // Warning yellow
                case "COMPLETADA" -> "#0dcaf0"; // Info cyan
                case "CANCELADA" -> "#dc3545"; // Danger red
                default -> "#6c757d";
            };
            
            String title = cita.getPaciente().getUsuario().getNombre() + " " + cita.getPaciente().getUsuario().getApellido();
            String start = cita.getFecha().toString() + "T" + cita.getHoraInicio().toString();
            String end = cita.getFecha().toString() + "T" + cita.getHoraFin().toString();
            
            return com.sisol.salud.dto.response.CalendarEventDto.builder()
                .id(cita.getId())
                .title(title)
                .start(start)
                .end(end)
                .color(color)
                .extendedProps(cita.getEstado().name())
                .build();
        }).collect(java.util.stream.Collectors.toList());
    }
    
    @org.springframework.web.bind.annotation.PostMapping("/perfil/foto")
    @PreAuthorize("hasRole('MEDICO')")
    public String subirFotoPerfil(
            @org.springframework.web.bind.annotation.RequestParam("foto") org.springframework.web.multipart.MultipartFile foto,
            java.security.Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        if (principal != null && !foto.isEmpty()) {
            try {
                String contentType = foto.getContentType();
                if (contentType != null && contentType.startsWith("image/")) {
                    byte[] bytes = foto.getBytes();
                    String base64Image = "data:" + contentType + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
                    
                    com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
                    if (usuario != null) {
                        usuario.setFotoPerfil(base64Image);
                        usuarioRepository.save(usuario);
                        redirectAttributes.addFlashAttribute("mensajeExito", "Foto de perfil actualizada correctamente");
                    }
                } else {
                    redirectAttributes.addFlashAttribute("mensajeError", "El archivo debe ser una imagen");
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("mensajeError", "Error al procesar la imagen");
            }
        }
        return "redirect:/panel-medico/disponibilidad";
    }

    @GetMapping("/perfil/foto/eliminar")
    @PreAuthorize("hasRole('MEDICO')")
    public String eliminarFotoPerfil(
            java.security.Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        if (principal != null) {
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            if (usuario != null) {
                usuario.setFotoPerfil(null);
                usuarioRepository.save(usuario);
                redirectAttributes.addFlashAttribute("mensajeExito", "Foto de perfil eliminada correctamente");
            }
        }
        return "redirect:/panel-medico/disponibilidad";
    }

}
