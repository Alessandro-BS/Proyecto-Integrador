package com.sisol.salud.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Inicio");
        return "index";
    }

    @GetMapping("/nosotros")
    public String nosotros(Model model) {
        model.addAttribute("title", "Nosotros");
        return "nosotros";
    }

    @GetMapping("/servicios")
    public String servicios(Model model) {
        model.addAttribute("title", "Servicios");
        return "servicios";
    }
}
