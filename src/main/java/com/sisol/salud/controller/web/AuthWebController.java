package com.sisol.salud.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthWebController {

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("title", "Iniciar Sesión");
        return "auth/login";
    }

    @GetMapping("/registro")
    public String registroPage(Model model) {
        model.addAttribute("title", "Crear Cuenta");
        return "auth/registro";
    }
}
