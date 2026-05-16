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

    @GetMapping
    public String listarMedicos(Model model) {
        List<MedicoResponse> medicos = medicoService.obtenerTodos();
        model.addAttribute("medicos", medicos);
        model.addAttribute("title", "Nuestros Médicos");
        return "medicos";
    }
}
