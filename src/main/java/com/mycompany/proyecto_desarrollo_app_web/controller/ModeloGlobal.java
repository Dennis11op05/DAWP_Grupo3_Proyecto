package com.mycompany.proyecto_desarrollo_app_web.controller;

import com.mycompany.proyecto_desarrollo_app_web.repository.UsuarioRepository;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class ModeloGlobal {
    private final UsuarioRepository usuarios;
    @ModelAttribute
    public void identidad(Model model) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean autentico = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        model.addAttribute("rolesActuales", autentico ? auth.getAuthorities().stream()
                .map(a -> a.getAuthority()).filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5)).collect(Collectors.toSet()) : Set.of());
        model.addAttribute("correoActual", autentico ? auth.getName() : "");
        model.addAttribute("nombreActual", autentico ? usuarios.findByCorreoIgnoreCase(auth.getName())
                .map(u -> u.getNombre()).orElse(auth.getName()) : "");
    }
}
