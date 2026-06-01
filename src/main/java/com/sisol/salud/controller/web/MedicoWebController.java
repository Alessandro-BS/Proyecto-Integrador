package com.sisol.salud.controller.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sisol.salud.dto.response.MedicoResponse;
import com.sisol.salud.service.MedicoService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/medicos")
@RequiredArgsConstructor
public class MedicoWebController {

    private final MedicoService medicoService;
    private final com.sisol.salud.repository.EspecialidadRepository especialidadRepository;

    @GetMapping
    public String listarMedicos(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long especialidadId,
            Model model) {
        
        List<MedicoResponse> medicos;
        if ((keyword != null && !keyword.trim().isEmpty()) || especialidadId != null) {
            medicos = medicoService.buscarPorFiltros(keyword, especialidadId);
        } else {
            medicos = medicoService.obtenerTodos();
        }
        
        model.addAttribute("medicos", medicos);
        model.addAttribute("especialidades", especialidadRepository.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("especialidadId", especialidadId);
        model.addAttribute("title", "Nuestros Médicos");
        return "medicos";
    }
}
