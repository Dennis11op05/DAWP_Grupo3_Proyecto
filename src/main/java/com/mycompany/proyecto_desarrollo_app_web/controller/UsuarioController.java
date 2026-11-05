package com.mycompany.proyecto_desarrollo_app_web.controller;

import com.mycompany.proyecto_desarrollo_app_web.domain.UsuarioFormulario;
import com.mycompany.proyecto_desarrollo_app_web.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuario")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarios;
    @GetMapping("/listado")
    public String listado(Model model) {
        model.addAttribute("formulario", new UsuarioFormulario());
        return cargar(model);
    }
    @GetMapping("/modificar/{id}")
    public String modificar(@PathVariable Long id, Model model) {
        model.addAttribute("formulario", usuarios.formulario(id));
        return cargar(model);
    }
    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("formulario") UsuarioFormulario formulario,
            BindingResult errores, Model model, RedirectAttributes flash) {
        if (!errores.hasErrors()) {
            try {
                usuarios.guardar(formulario);
                flash.addFlashAttribute("todoOk", "Usuario y permisos guardados correctamente.");
                return "redirect:/usuario/listado";
            } catch (IllegalArgumentException | DataIntegrityViolationException e) {
                errores.reject("usuario", e instanceof IllegalArgumentException ? e.getMessage() : "El correo ya está registrado.");
            }
        }
        formulario.setPassword("");
        return cargar(model);
    }
    private String cargar(Model model) {
        model.addAttribute("usuarios", usuarios.listar());
        model.addAttribute("roles", usuarios.roles());
        return "usuario/listado";
    }
}
