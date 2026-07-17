package com.sisol.salud.controller.web;

import com.sisol.salud.model.entity.Cita;
import com.sisol.salud.model.entity.Medico;
import com.sisol.salud.model.entity.Usuario;
import com.sisol.salud.model.enums.EstadoCita;
import com.sisol.salud.model.enums.Rol;
import com.sisol.salud.repository.CitaRepository;
import com.sisol.salud.repository.EspecialidadRepository;
import com.sisol.salud.repository.MedicoRepository;
import com.sisol.salud.repository.PacienteRepository;
import com.sisol.salud.repository.UsuarioRepository;
import com.sisol.salud.repository.DisponibilidadMedicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sisol.salud.model.entity.DisponibilidadMedica;
import com.sisol.salud.model.enums.DiaSemana;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminWebController {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final EspecialidadRepository especialidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final DisponibilidadMedicaRepository disponibilidadMedicaRepository;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String dashboard(@RequestParam(required = false, defaultValue = "week") String period, Principal principal, Model model) {
        if (principal != null) {
            Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            model.addAttribute("usuario", usuario);
        }

        List<Cita> todasLasCitas = citaRepository.findAll();
        
        LocalDate startDate;
        if ("year".equals(period)) {
            startDate = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        } else if ("month".equals(period)) {
            startDate = LocalDate.now().minusDays(30);
        } else {
            startDate = LocalDate.now().minusDays(7);
        }
        
        List<Cita> citasFiltradas = todasLasCitas.stream()
            .filter(c -> c.getCreatedAt() != null && !c.getCreatedAt().toLocalDate().isBefore(startDate))
            .collect(Collectors.toList());

        long totalCitas = citasFiltradas.size();
        
        long pacientesAtendidos = citasFiltradas.stream()
            .map(c -> c.getPaciente().getId())
            .distinct()
            .count();
            
        long medicosActivos = medicoRepository.count();
        
        long citasCompletadas = citasFiltradas.stream()
            .filter(c -> c.getEstado() == EstadoCita.COMPLETADA)
            .count();

        // Obtener últimas 10 citas para la tabla
        List<Cita> ultimasCitas = citasFiltradas.stream()
            .sorted((c1, c2) -> c2.getFecha().compareTo(c1.getFecha()))
            .limit(10)
            .collect(Collectors.toList());

        model.addAttribute("totalCitas", totalCitas);
        model.addAttribute("pacientesAtendidos", pacientesAtendidos);
        model.addAttribute("medicosActivos", medicosActivos);
        model.addAttribute("citasCompletadas", citasCompletadas);
        model.addAttribute("ultimasCitas", ultimasCitas);
        model.addAttribute("period", period);
        model.addAttribute("title", "Panel de Administración");
        
        return "admin/dashboard";
    }

    @GetMapping("/api/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats(@RequestParam(required = false, defaultValue = "week") String period) {
        Map<String, Object> stats = new HashMap<>();
        List<Cita> citas = citaRepository.findAll();

        Map<String, Long> citasData = new HashMap<>();

        if ("year".equals(period)) {
            // Últimos 12 meses
            for (int i = 11; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusMonths(i).withDayOfMonth(1);
                String monthLabel = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
                long count = citas.stream()
                    .filter(c -> c.getCreatedAt() != null && 
                                 c.getCreatedAt().getYear() == date.getYear() &&
                                 c.getCreatedAt().getMonthValue() == date.getMonthValue())
                    .count();
                citasData.put(monthLabel, count);
            }
        } else if ("month".equals(period)) {
            // Últimos 30 días
            for (int i = 29; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                long count = citas.stream()
                    .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().toLocalDate().equals(date))
                    .count();
                citasData.put(date.toString(), count);
            }
        } else {
            // Últimos 7 días
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                long count = citas.stream()
                    .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().toLocalDate().equals(date))
                    .count();
                citasData.put(date.toString(), count);
            }
        }
        
        stats.put("citasPorDia", citasData);

        // Datos para gráfico de Dona (Especialidades)
        Map<String, Long> citasPorEspecialidad = citas.stream()
            .filter(c -> c.getEspecialidad() != null)
            .collect(Collectors.groupingBy(
                c -> c.getEspecialidad().getNombre(),
                Collectors.counting()
            ));
        stats.put("citasPorEspecialidad", citasPorEspecialidad);

        return ResponseEntity.ok(stats);
    }

    // ==========================================
    // CRUD DE MÉDICOS
    // ==========================================

    @GetMapping("/medicos")
    @PreAuthorize("hasRole('ADMIN')")
    public String listarMedicos(
            @RequestParam(required = false) String search,
            Principal principal, 
            Model model) {
        
        if (principal != null) {
            Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            model.addAttribute("usuario", usuario);
        }

        List<Medico> medicos = medicoRepository.findAll();
        
        if (search != null && !search.trim().isEmpty()) {
            String term = search.toLowerCase();
            medicos = medicos.stream()
                .filter(m -> m.getUsuario().getNombre().toLowerCase().contains(term) ||
                             m.getUsuario().getApellido().toLowerCase().contains(term) ||
                             m.getNumeroColegiatura().toLowerCase().contains(term) ||
                             m.getEspecialidades().stream().anyMatch(e -> e.getNombre().toLowerCase().contains(term)))
                .collect(Collectors.toList());
        }

        model.addAttribute("medicos", medicos);
        model.addAttribute("search", search);
        model.addAttribute("title", "Gestión de Médicos");
        return "admin/medicos";
    }

    @GetMapping("/medicos/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevoMedico(Principal principal, Model model) {
        if (principal != null) {
            Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            model.addAttribute("usuario", usuario);
        }
        model.addAttribute("medico", new Medico());
        model.addAttribute("usuarioMedico", new Usuario());
        model.addAttribute("especialidadesTodas", especialidadRepository.findAll());
        model.addAttribute("title", "Registrar Médico");
        return "admin/medico-form";
    }

    @PostMapping("/medicos/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public String guardarMedico(
            @ModelAttribute("usuarioMedico") Usuario usuarioMedico,
            @RequestParam("numeroColegiatura") String numeroColegiatura,
            @RequestParam(value = "especialidadIds", required = false) List<Long> especialidadIds,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Verificar si el email o DNI ya existe
            if (usuarioRepository.findByEmail(usuarioMedico.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El correo electrónico ya está registrado.");
                return "redirect:/admin/medicos/nuevo";
            }

            // 1. Guardar Usuario
            usuarioMedico.setRol(Rol.MEDICO);
            usuarioMedico.setActivo(true);
            usuarioMedico.setPassword(passwordEncoder.encode(usuarioMedico.getPassword()));
            Usuario savedUser = usuarioRepository.save(usuarioMedico);

            // 2. Obtener Especialidades
            Set<com.sisol.salud.model.entity.Especialidad> especialidades = new HashSet<>();
            if (especialidadIds != null && !especialidadIds.isEmpty()) {
                for (Long id : especialidadIds) {
                    especialidadRepository.findById(id).ifPresent(especialidades::add);
                }
            }

            // 3. Crear y Guardar Médico
            Medico medico = Medico.builder()
                    .usuario(savedUser)
                    .numeroColegiatura(numeroColegiatura)
                    .especialidades(especialidades)
                    .build();
            medico = medicoRepository.save(medico);

            // 4. Crear Horario por defecto (L-V, 8-17)
            for (DiaSemana dia : DiaSemana.values()) {
                if (dia != DiaSemana.SABADO && dia != DiaSemana.DOMINGO) {
                    DisponibilidadMedica disp = DisponibilidadMedica.builder()
                            .medico(medico)
                            .diaSemana(dia)
                            .horaInicio(LocalTime.of(8, 0))
                            .horaFin(LocalTime.of(17, 0))
                            .duracionConsultaMin(30)
                            .activo(true)
                            .build();
                    disponibilidadMedicaRepository.save(disp);
                }
            }

            redirectAttributes.addFlashAttribute("success", "Médico registrado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ocurrió un error al registrar al médico.");
        }
        
        return "redirect:/admin/medicos";
    }

    @GetMapping("/medicos/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editarMedico(@PathVariable Long id, Principal principal, Model model) {
        if (principal != null) {
            Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            model.addAttribute("usuario", usuario);
        }
        
        Medico medico = medicoRepository.findById(id).orElse(null);
        if (medico == null) {
            return "redirect:/admin/medicos";
        }

        model.addAttribute("medico", medico);
        model.addAttribute("usuarioMedico", medico.getUsuario());
        model.addAttribute("especialidadesTodas", especialidadRepository.findAll());
        model.addAttribute("title", "Editar Médico");
        return "admin/medico-form";
    }

    @PostMapping("/medicos/actualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public String actualizarMedico(
            @RequestParam("medicoId") Long medicoId,
            @ModelAttribute("usuarioMedico") Usuario formUser,
            @RequestParam("numeroColegiatura") String numeroColegiatura,
            @RequestParam(value = "especialidadIds", required = false) List<Long> especialidadIds,
            RedirectAttributes redirectAttributes) {
        
        try {
            Medico medico = medicoRepository.findById(medicoId).orElse(null);
            if (medico != null) {
                Usuario userToUpdate = medico.getUsuario();
                
                // Actualizar Usuario
                userToUpdate.setNombre(formUser.getNombre());
                userToUpdate.setApellido(formUser.getApellido());
                userToUpdate.setTelefono(formUser.getTelefono());
                
                // Si la contraseña no está vacía, se actualiza
                if (formUser.getPassword() != null && !formUser.getPassword().trim().isEmpty()) {
                    userToUpdate.setPassword(passwordEncoder.encode(formUser.getPassword()));
                }
                usuarioRepository.save(userToUpdate);

                // Actualizar Especialidades
                Set<com.sisol.salud.model.entity.Especialidad> especialidades = new HashSet<>();
                if (especialidadIds != null && !especialidadIds.isEmpty()) {
                    for (Long id : especialidadIds) {
                        especialidadRepository.findById(id).ifPresent(especialidades::add);
                    }
                }
                medico.setEspecialidades(especialidades);
                medico.setNumeroColegiatura(numeroColegiatura);
                medicoRepository.save(medico);

                redirectAttributes.addFlashAttribute("success", "Datos del médico actualizados.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar al médico.");
        }
        
        return "redirect:/admin/medicos";
    }

    @GetMapping("/medicos/toggle-status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public String toggleStatusMedico(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Medico medico = medicoRepository.findById(id).orElse(null);
        if (medico != null) {
            Usuario usuario = medico.getUsuario();
            usuario.setActivo(!usuario.isActivo());
            usuarioRepository.save(usuario);
            String action = usuario.isActivo() ? "activado" : "desactivado";
            redirectAttributes.addFlashAttribute("success", "Médico " + action + " correctamente.");
        }
        return "redirect:/admin/medicos";
    }
    @GetMapping("/pacientes")
    @PreAuthorize("hasRole('ADMIN')")
    public String listarPacientes(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            model.addAttribute("usuario", usuario);
        }
        
        List<com.sisol.salud.model.entity.Paciente> pacientes = pacienteRepository.findAll();
        model.addAttribute("pacientes", pacientes);
        model.addAttribute("title", "Gestión de Pacientes");
        return "admin/pacientes";
    }

    @GetMapping("/pacientes/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevoPaciente(Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            model.addAttribute("usuario", usuario);
        }
        model.addAttribute("paciente", new com.sisol.salud.model.entity.Paciente());
        model.addAttribute("usuarioPaciente", new Usuario());
        model.addAttribute("title", "Registrar Paciente");
        return "admin/paciente-form";
    }

    @PostMapping("/pacientes/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public String guardarPaciente(@ModelAttribute Usuario formUser, RedirectAttributes redirectAttributes) {
        try {
            if (usuarioRepository.findByEmail(formUser.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El correo ya está en uso.");
                return "redirect:/admin/pacientes/nuevo";
            }
            if (usuarioRepository.findByDni(formUser.getDni()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "El DNI ya está registrado.");
                return "redirect:/admin/pacientes/nuevo";
            }

            formUser.setPassword(passwordEncoder.encode(formUser.getPassword()));
            formUser.setRol(Rol.PACIENTE);
            formUser.setActivo(true);
            Usuario savedUser = usuarioRepository.save(formUser);

            com.sisol.salud.model.entity.Paciente paciente = new com.sisol.salud.model.entity.Paciente();
            paciente.setUsuario(savedUser);
            pacienteRepository.save(paciente);

            redirectAttributes.addFlashAttribute("success", "Paciente registrado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el paciente.");
        }
        return "redirect:/admin/pacientes";
    }

    @GetMapping("/pacientes/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editarPaciente(@PathVariable Long id, Model model, Principal principal) {
        if (principal != null) {
            Usuario usuario = usuarioRepository.findByEmail(principal.getName()).orElse(null);
            model.addAttribute("usuario", usuario);
        }
        com.sisol.salud.model.entity.Paciente paciente = pacienteRepository.findById(id).orElse(null);
        if (paciente == null) {
            return "redirect:/admin/pacientes";
        }
        model.addAttribute("paciente", paciente);
        model.addAttribute("usuarioPaciente", paciente.getUsuario());
        model.addAttribute("title", "Editar Paciente");
        return "admin/paciente-form";
    }

    @PostMapping("/pacientes/actualizar")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public String actualizarPaciente(@RequestParam Long pacienteId, @ModelAttribute Usuario formUser, RedirectAttributes redirectAttributes) {
        try {
            com.sisol.salud.model.entity.Paciente paciente = pacienteRepository.findById(pacienteId).orElse(null);
            if (paciente != null) {
                Usuario userToUpdate = paciente.getUsuario();
                
                userToUpdate.setNombre(formUser.getNombre());
                userToUpdate.setApellido(formUser.getApellido());
                userToUpdate.setTelefono(formUser.getTelefono());
                
                if (formUser.getPassword() != null && !formUser.getPassword().trim().isEmpty()) {
                    userToUpdate.setPassword(passwordEncoder.encode(formUser.getPassword()));
                }
                usuarioRepository.save(userToUpdate);
                
                redirectAttributes.addFlashAttribute("success", "Datos del paciente actualizados.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar al paciente.");
        }
        return "redirect:/admin/pacientes";
    }

    @GetMapping("/pacientes/toggle-status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public String toggleStatusPaciente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        com.sisol.salud.model.entity.Paciente paciente = pacienteRepository.findById(id).orElse(null);
        if (paciente != null) {
            Usuario usuario = paciente.getUsuario();
            usuario.setActivo(!usuario.isActivo());
            usuarioRepository.save(usuario);
            String action = usuario.isActivo() ? "activado" : "desactivado";
            redirectAttributes.addFlashAttribute("success", "Paciente " + action + " correctamente.");
        }
        return "redirect:/admin/pacientes";
    }
}
