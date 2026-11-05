package com.mycompany.proyecto_desarrollo_app_web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.mycompany.proyecto_desarrollo_app_web.service.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class IndexController {
    private final ActorService actor;
    private final ConciliacionService conciliaciones;
    private final ImportacionService importaciones;
    private final UsuarioService usuarios;

    @GetMapping("/")
    public String inicio(Model model) {
        Set<String> roles = actor.actual().getRoles().stream().map(r -> r.getCodigo()).collect(Collectors.toSet());
        boolean operaciones = roles.contains("ANALISTA") || roles.contains("SUPERVISOR");
        var lista = operaciones ? conciliaciones.listar("", "") : List.<com.mycompany.proyecto_desarrollo_app_web.domain.Conciliacion>of();
        var diferencias = operaciones ? conciliaciones.todasDiferencias() : List.<com.mycompany.proyecto_desarrollo_app_web.domain.Diferencia>of();
        model.addAttribute("conciliaciones", lista);
        model.addAttribute("abiertas", lista.stream().filter(c -> c.getEstado().equals("ABIERTA")).count());
        model.addAttribute("cerradas", lista.stream().filter(c -> c.getEstado().equals("CERRADA")).count());
        model.addAttribute("pendientes", diferencias.stream().filter(d -> !d.getEstado().equals("RESUELTA")).count());
        model.addAttribute("requierenAtencion", diferencias.stream().filter(d -> !d.getEstado().equals("RESUELTA")).map(d -> d.getConciliacion().getId()).collect(Collectors.toSet()));
        if (roles.contains("ADMIN")) {
            model.addAttribute("totalUsuarios", usuarios.listar().size());
            model.addAttribute("totalImportaciones", importaciones.historial().size());
        }
        return "index";
    }
    @GetMapping("/login")
    public String login() { return "login"; }
}
