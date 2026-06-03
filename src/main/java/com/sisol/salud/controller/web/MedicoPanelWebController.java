package com.sisol.salud.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/panel-medico")
public class MedicoPanelWebController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('MEDICO')")
    public String dashboard(Model model) {
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
