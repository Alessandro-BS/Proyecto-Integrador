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
    private final com.sisol.salud.repository.CitaRepository citaRepository;
    private final com.sisol.salud.repository.EspecialidadRepository especialidadRepository;
    private final com.sisol.salud.repository.MedicoRepository medicoRepository;
    private final com.sisol.salud.repository.DisponibilidadMedicaRepository disponibilidadMedicaRepository;
    private final com.sisol.salud.service.NotificacionService notificacionService;
    private final com.sisol.salud.repository.NotificacionRepository notificacionRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final com.sisol.salud.service.PdfReportService pdfReportService;

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
                
                if (paciente != null) {
                    java.util.List<com.sisol.salud.model.entity.Cita> citas = citaRepository.findByPacienteId(paciente.getId());
                    java.util.List<com.sisol.salud.model.entity.Cita> proximasCitas = citas.stream()
                        .filter(c -> c.getFecha().isAfter(java.time.LocalDate.now().minusDays(1)) && 
                                    (c.getEstado() == com.sisol.salud.model.enums.EstadoCita.PENDIENTE || 
                                     c.getEstado() == com.sisol.salud.model.enums.EstadoCita.CONFIRMADA))
                        .sorted(java.util.Comparator.comparing(com.sisol.salud.model.entity.Cita::getFecha).thenComparing(com.sisol.salud.model.entity.Cita::getHoraInicio))
                        .limit(3)
                        .collect(java.util.stream.Collectors.toList());
                        
                    model.addAttribute("proximasCitas", proximasCitas);
                    model.addAttribute("totalCitas", citas.size());
                }
            }
        }
        
        model.addAttribute("title", "Mi Panel - Paciente");
        return "paciente/dashboard";
    }

    @GetMapping("/mis-citas")
    @PreAuthorize("hasRole('PACIENTE')")
    public String misCitas(java.security.Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                com.sisol.salud.model.entity.Paciente paciente = pacienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
                if (paciente != null) {
                    java.util.List<com.sisol.salud.model.entity.Cita> todasLasCitas = citaRepository.findByPacienteId(paciente.getId());
                    
                    java.util.List<com.sisol.salud.model.entity.Cita> proximas = todasLasCitas.stream()
                        .filter(c -> c.getFecha().isAfter(java.time.LocalDate.now().minusDays(1)) && 
                                    (c.getEstado() == com.sisol.salud.model.enums.EstadoCita.PENDIENTE || 
                                     c.getEstado() == com.sisol.salud.model.enums.EstadoCita.CONFIRMADA))
                        .sorted(java.util.Comparator.comparing(com.sisol.salud.model.entity.Cita::getFecha).thenComparing(com.sisol.salud.model.entity.Cita::getHoraInicio))
                        .collect(java.util.stream.Collectors.toList());
                        
                    java.util.List<com.sisol.salud.model.entity.Cita> pasadas = todasLasCitas.stream()
                        .filter(c -> c.getEstado() == com.sisol.salud.model.enums.EstadoCita.COMPLETADA || 
                                     c.getEstado() == com.sisol.salud.model.enums.EstadoCita.NO_ASISTIO ||
                                     (c.getFecha().isBefore(java.time.LocalDate.now()) && c.getEstado() != com.sisol.salud.model.enums.EstadoCita.CANCELADA))
                        .sorted(java.util.Comparator.comparing(com.sisol.salud.model.entity.Cita::getFecha).reversed())
                        .collect(java.util.stream.Collectors.toList());
                        
                    java.util.List<com.sisol.salud.model.entity.Cita> canceladas = todasLasCitas.stream()
                        .filter(c -> c.getEstado() == com.sisol.salud.model.enums.EstadoCita.CANCELADA)
                        .sorted(java.util.Comparator.comparing(com.sisol.salud.model.entity.Cita::getFecha).reversed())
                        .collect(java.util.stream.Collectors.toList());

                    model.addAttribute("citasProximas", proximas);
                    model.addAttribute("citasPasadas", pasadas);
                    model.addAttribute("citasCanceladas", canceladas);
                }
            }
        }
        model.addAttribute("title", "Mis Citas");
        return "paciente/mis-citas";
    }

    @GetMapping("/reservar-cita")
    @PreAuthorize("hasRole('PACIENTE')")
    public String reservarCitaPaso1(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int espPage,
            Model model) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(espPage, 4);
        org.springframework.data.domain.Page<com.sisol.salud.model.entity.Especialidad> especialidadesPage = especialidadRepository.findByActivoTrue(pageable);
        model.addAttribute("especialidadesPage", especialidadesPage);
        model.addAttribute("especialidades", especialidadesPage.getContent());
        model.addAttribute("title", "Selecciona una Especialidad");
        return "paciente/reservar-paso1";
    }

    @PostMapping("/reservar-cita/paso2")
    @PreAuthorize("hasRole('PACIENTE')")
    public String procesarPaso1(
            @org.springframework.web.bind.annotation.RequestParam("especialidadId") Long especialidadId,
            Model model) {
        com.sisol.salud.model.entity.Especialidad especialidad = especialidadRepository.findById(especialidadId).orElse(null);
        java.util.List<com.sisol.salud.model.entity.Medico> medicos = medicoRepository.findByEspecialidades_Id(especialidadId);
        
        model.addAttribute("especialidad", especialidad);
        model.addAttribute("medicos", medicos);
        model.addAttribute("title", "Selecciona un Especialista");
        return "paciente/reservar-paso2";
    }

    @PostMapping("/reservar-cita/paso3")
    @PreAuthorize("hasRole('PACIENTE')")
    public String procesarPaso2(
            @org.springframework.web.bind.annotation.RequestParam("especialidadId") Long especialidadId,
            @org.springframework.web.bind.annotation.RequestParam("medicoId") Long medicoId,
            Model model) {
        com.sisol.salud.model.entity.Especialidad especialidad = especialidadRepository.findById(especialidadId).orElse(null);
        com.sisol.salud.model.entity.Medico medico = medicoRepository.findById(medicoId).orElse(null);
        java.util.List<com.sisol.salud.model.entity.DisponibilidadMedica> disponibilidades = disponibilidadMedicaRepository.findByMedicoId(medicoId);
        
        model.addAttribute("especialidad", especialidad);
        model.addAttribute("medico", medico);
        model.addAttribute("disponibilidades", disponibilidades);
        model.addAttribute("title", "Selecciona Fecha y Horario");
        return "paciente/reservar-paso3";
    }

    @PostMapping("/reservar-cita/finalizar")
    @PreAuthorize("hasRole('PACIENTE')")
    public String finalizarReserva(
            @org.springframework.web.bind.annotation.RequestParam("especialidadId") Long especialidadId,
            @org.springframework.web.bind.annotation.RequestParam("medicoId") Long medicoId,
            @org.springframework.web.bind.annotation.RequestParam("fecha") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fecha,
            @org.springframework.web.bind.annotation.RequestParam("hora") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.TIME) java.time.LocalTime hora,
            @org.springframework.web.bind.annotation.RequestParam("metodoPago") String metodoPago,
            java.security.Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        System.out.println("====== INICIANDO FINALIZAR RESERVA ======");
        System.out.println("EspecialidadId: " + especialidadId);
        System.out.println("MedicoId: " + medicoId);
        System.out.println("Fecha: " + fecha);
        System.out.println("Hora: " + hora);
        System.out.println("MetodoPago: " + metodoPago);
        System.out.println("Principal name: " + (principal != null ? principal.getName() : "null"));

        com.sisol.salud.model.entity.Paciente paciente = null;
        if (principal != null) {
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            System.out.println("Usuario encontrado: " + (usuario != null ? usuario.getId() : "null"));
            if (usuario != null) {
                paciente = pacienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
                
                if (paciente == null && usuario.getRol() == com.sisol.salud.model.enums.Rol.PACIENTE) {
                    System.out.println("Paciente no encontrado. Creando registro de Paciente automáticamente...");
                    paciente = new com.sisol.salud.model.entity.Paciente();
                    paciente.setUsuario(usuario);
                    paciente = pacienteRepository.save(paciente);
                }
                
                System.out.println("Paciente encontrado/creado: " + (paciente != null ? paciente.getId() : "null"));
            }
        }

        if (paciente != null) {
            com.sisol.salud.model.entity.Especialidad especialidad = especialidadRepository.findById(especialidadId).orElse(null);
            com.sisol.salud.model.entity.Medico medico = medicoRepository.findById(medicoId).orElse(null);
            System.out.println("Especialidad encontrada: " + (especialidad != null ? especialidad.getNombre() : "null"));
            System.out.println("Medico encontrado: " + (medico != null ? medico.getId() : "null"));

            if (especialidad != null && medico != null) {
                try {
                    com.sisol.salud.model.entity.Cita nuevaCita = new com.sisol.salud.model.entity.Cita();
                    nuevaCita.setPaciente(paciente);
                    nuevaCita.setMedico(medico);
                    nuevaCita.setEspecialidad(especialidad);
                    nuevaCita.setFecha(fecha);
                    nuevaCita.setHoraInicio(hora);
                    nuevaCita.setHoraFin(hora.plusMinutes(30));
                    nuevaCita.setEstado(com.sisol.salud.model.enums.EstadoCita.PENDIENTE);
                    nuevaCita.setMotivoConsulta("Reserva por portal web");

                    System.out.println("Guardando cita...");
                    citaRepository.save(nuevaCita);
                    System.out.println("Cita guardada con éxito. ID: " + nuevaCita.getId());

                    // Enviar correo de confirmación de forma asíncrona usando la arquitectura existente
                    if (paciente.getUsuario().getEmail() != null) {
                        notificacionService.enviarConfirmacionCita(nuevaCita);
                    }

                    redirectAttributes.addFlashAttribute("mensajeExito", 
                        "¡Cita reservada exitosamente para el " + fecha.toString() + " a las " + hora.toString() + " con el doctor " + medico.getUsuario().getNombre() + " " + medico.getUsuario().getApellido() + "!");
                } catch (Exception e) {
                    System.err.println("Error al guardar cita: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Especialidad o medico son nulos.");
            }
        } else {
            System.out.println("Paciente es nulo.");
        }
        System.out.println("====== FIN FINALIZAR RESERVA ======");
        
        return "redirect:/paciente/mis-citas";
    }

    @GetMapping("/resultados")
    @PreAuthorize("hasRole('PACIENTE')")
    public String resultadosMedicos(java.security.Principal principal, Model model) {
        if (principal != null) {
            String email = principal.getName();
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                com.sisol.salud.model.entity.Paciente paciente = pacienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
                if (paciente != null) {
                    java.util.List<com.sisol.salud.model.entity.Cita> citas = citaRepository.findByPacienteId(paciente.getId());
                    java.util.List<com.sisol.salud.model.entity.Cita> resultados = citas.stream()
                        .filter(c -> c.getEstado() == com.sisol.salud.model.enums.EstadoCita.COMPLETADA || 
                                     c.getEstado() == com.sisol.salud.model.enums.EstadoCita.PENDIENTE || 
                                     c.getEstado() == com.sisol.salud.model.enums.EstadoCita.CONFIRMADA)
                        .sorted(java.util.Comparator.comparing(com.sisol.salud.model.entity.Cita::getFecha).reversed())
                        .collect(java.util.stream.Collectors.toList());
                    model.addAttribute("resultados", resultados);
                }
            }
        }
        model.addAttribute("title", "Resultados Médicos");
        return "paciente/resultados";
    }
    
    @GetMapping("/informe/{id}")
    @PreAuthorize("hasRole('PACIENTE')")
    public org.springframework.http.ResponseEntity<byte[]> descargarInformePdf(@org.springframework.web.bind.annotation.PathVariable Long id, java.security.Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                com.sisol.salud.model.entity.Paciente paciente = pacienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
                if (paciente != null) {
                    com.sisol.salud.model.entity.Cita cita = citaRepository.findById(id).orElse(null);
                    // Validar que la cita pertenece al paciente y está completada
                    if (cita != null && cita.getPaciente().getId().equals(paciente.getId()) && cita.getEstado() == com.sisol.salud.model.enums.EstadoCita.COMPLETADA) {
                        
                        byte[] pdfBytes = pdfReportService.generarInformeCita(cita);
                        
                        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
                        headers.setContentDispositionFormData("attachment", "informe_cita_" + id + ".pdf");
                        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
                        
                        return new org.springframework.http.ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
                    }
                }
            }
        }
        return new org.springframework.http.ResponseEntity<>(org.springframework.http.HttpStatus.FORBIDDEN);
    }

    @GetMapping("/perfil")
    @PreAuthorize("hasRole('PACIENTE')")
    public String perfil(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int notifPage,
            java.security.Principal principal, 
            Model model) {
        if (principal != null) {
            String email = principal.getName();
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                com.sisol.salud.model.entity.Paciente paciente = pacienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
                model.addAttribute("usuario", usuario);
                model.addAttribute("paciente", paciente);
                
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(notifPage, 5);
                org.springframework.data.domain.Page<com.sisol.salud.model.entity.Notificacion> notificacionesPage = notificacionRepository.findByUsuarioIdOrderByFechaEnvioDesc(usuario.getId(), pageable);
                model.addAttribute("notificacionesPage", notificacionesPage);
                model.addAttribute("notificaciones", notificacionesPage.getContent());
                
                // Set the active tab to notifications if we are paginating
                if (notifPage > 0) {
                    model.addAttribute("activeTab", "notificaciones");
                } else {
                    model.addAttribute("activeTab", "perfil");
                }
            }
        }
        model.addAttribute("title", "Editar Perfil");
        return "paciente/perfil";
    }

    @PostMapping("/perfil/actualizar")
    @PreAuthorize("hasRole('PACIENTE')")
    public String actualizarPerfil(
            @org.springframework.web.bind.annotation.RequestParam("telefono") String telefono,
            @org.springframework.web.bind.annotation.RequestParam("fechaNacimiento") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fechaNacimiento,
            @org.springframework.web.bind.annotation.RequestParam("genero") String genero,
            @org.springframework.web.bind.annotation.RequestParam(value = "contactoEmergenciaNombre", required = false) String contactoEmergenciaNombre,
            @org.springframework.web.bind.annotation.RequestParam(value = "contactoEmergenciaParentesco", required = false) String contactoEmergenciaParentesco,
            @org.springframework.web.bind.annotation.RequestParam(value = "contactoEmergenciaTelefono", required = false) String contactoEmergenciaTelefono,
            java.security.Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        if (principal != null) {
            String email = principal.getName();
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                usuario.setTelefono(telefono);
                usuarioRepository.save(usuario);
                
                com.sisol.salud.model.entity.Paciente paciente = pacienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
                if (paciente != null) {
                    paciente.setFechaNacimiento(fechaNacimiento);
                    paciente.setGenero(genero);
                    paciente.setContactoEmergenciaNombre(contactoEmergenciaNombre);
                    paciente.setContactoEmergenciaParentesco(contactoEmergenciaParentesco);
                    paciente.setContactoEmergenciaTelefono(contactoEmergenciaTelefono);
                    pacienteRepository.save(paciente);
                }
            }
        }
        redirectAttributes.addFlashAttribute("mensajeExito", "Perfil actualizado correctamente");
        return "redirect:/paciente/perfil";
    }

    @PostMapping("/cita/cancelar")
    @PreAuthorize("hasRole('PACIENTE')")
    public String cancelarCita(
            @org.springframework.web.bind.annotation.RequestParam("citaId") Long citaId,
            @org.springframework.web.bind.annotation.RequestParam(value = "origen", defaultValue = "dashboard") String origen,
            java.security.Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        if (principal != null) {
            String email = principal.getName();
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            if (usuario != null) {
                com.sisol.salud.model.entity.Paciente paciente = pacienteRepository.findByUsuarioId(usuario.getId()).orElse(null);
                if (paciente != null) {
                    com.sisol.salud.model.entity.Cita cita = citaRepository.findById(citaId).orElse(null);
                    
                    if (cita != null && cita.getPaciente().getId().equals(paciente.getId()) && 
                        (cita.getEstado() == com.sisol.salud.model.enums.EstadoCita.PENDIENTE || 
                         cita.getEstado() == com.sisol.salud.model.enums.EstadoCita.CONFIRMADA)) {
                        
                        cita.setEstado(com.sisol.salud.model.enums.EstadoCita.CANCELADA);
                        citaRepository.save(cita);
                        redirectAttributes.addFlashAttribute("mensajeExito", "La cita ha sido cancelada correctamente.");
                    } else {
                        redirectAttributes.addFlashAttribute("mensajeError", "No se pudo cancelar la cita. Es posible que ya no esté disponible para cancelación.");
                    }
                }
            }
        }
        
        if ("miscitas".equals(origen)) {
            return "redirect:/paciente/mis-citas";
        }
        return "redirect:/paciente/dashboard";
    }

    @PostMapping("/perfil/foto")
    @PreAuthorize("hasRole('PACIENTE')")
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
        return "redirect:/paciente/perfil";
    }

    @GetMapping("/perfil/foto/eliminar")
    @PreAuthorize("hasRole('PACIENTE')")
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
        return "redirect:/paciente/perfil";
    }

    @PostMapping("/perfil/seguridad")
    @PreAuthorize("hasRole('PACIENTE')")
    public String cambiarPassword(
            @org.springframework.web.bind.annotation.RequestParam("actual") String actual,
            @org.springframework.web.bind.annotation.RequestParam("nueva") String nueva,
            @org.springframework.web.bind.annotation.RequestParam("confirmar") String confirmar,
            java.security.Principal principal,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        if (principal != null) {
            com.sisol.salud.model.entity.Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            if (usuario != null) {
                if (!passwordEncoder.matches(actual, usuario.getPassword())) {
                    redirectAttributes.addFlashAttribute("mensajeSeguridadError", "La contraseña actual es incorrecta");
                    return "redirect:/paciente/perfil?activeTab=seguridad";
                }
                
                if (!nueva.equals(confirmar)) {
                    redirectAttributes.addFlashAttribute("mensajeSeguridadError", "Las nuevas contraseñas no coinciden");
                    return "redirect:/paciente/perfil?activeTab=seguridad";
                }
                
                if (nueva.length() < 8) {
                    redirectAttributes.addFlashAttribute("mensajeSeguridadError", "La contraseña debe tener al menos 8 caracteres");
                    return "redirect:/paciente/perfil?activeTab=seguridad";
                }
                
                usuario.setPassword(passwordEncoder.encode(nueva));
                usuarioRepository.save(usuario);
                redirectAttributes.addFlashAttribute("mensajeSeguridadExito", "Contraseña actualizada correctamente");
            }
        }
        return "redirect:/paciente/perfil?activeTab=seguridad";
    }
}
