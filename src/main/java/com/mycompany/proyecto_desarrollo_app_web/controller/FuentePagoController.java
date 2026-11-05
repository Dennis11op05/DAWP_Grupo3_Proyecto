package com.mycompany.proyecto_desarrollo_app_web.controller;

import com.mycompany.proyecto_desarrollo_app_web.domain.FuentePago;
import com.mycompany.proyecto_desarrollo_app_web.service.FuentePagoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fuente-pago")
@RequiredArgsConstructor
public class FuentePagoController {

    private final FuentePagoService fuentePagoService;

    @GetMapping("/listado")
    public String listado(@RequestParam(defaultValue = "false") boolean nuevo, Model model) {
        var fuentes = cargarListado(model);
        model.addAttribute("fuentePago", !nuevo && !fuentes.isEmpty() ? fuentes.getFirst() : new FuentePago());
        return "fuente-pago/listado";
    }

    @GetMapping("/modificar/{idFuentePago}")
    public String modificar(@PathVariable Integer idFuentePago, Model model,
            RedirectAttributes redirectAttributes) {
        var fuente = fuentePagoService.getFuentePago(idFuentePago);
        if (fuente.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "La fuente de pago no existe.");
            return "redirect:/fuente-pago/listado";
        }
        model.addAttribute("fuentePago", fuente.get());
        cargarListado(model);
        return "fuente-pago/listado";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute FuentePago fuentePago,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                fuentePagoService.save(fuentePago);
                redirectAttributes.addFlashAttribute("todoOk", "La fuente de pago se guardó correctamente.");
                return "redirect:/fuente-pago/listado";
            } catch (IllegalArgumentException e) {
                bindingResult.reject("fuentePago", e.getMessage());
            } catch (DataIntegrityViolationException e) {
                bindingResult.reject("fuentePago", "No se pudo guardar. Compruebe que el código sea único.");
            }
        }
        // Conservamos los datos y errores del formulario para que el usuario pueda corregirlos.
        cargarListado(model);
        return "fuente-pago/listado";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idFuentePago, RedirectAttributes redirectAttributes) {
        try {
            fuentePagoService.delete(idFuentePago);
            redirectAttributes.addFlashAttribute("todoOk", "La fuente de pago se eliminó correctamente.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/fuente-pago/listado";
    }

    @PostMapping("/estado")
    public String cambiarEstado(@RequestParam Integer idFuentePago, @RequestParam boolean activo,
            RedirectAttributes redirectAttributes) {
        try {
            fuentePagoService.cambiarEstado(idFuentePago, activo);
            redirectAttributes.addFlashAttribute("todoOk", activo
                    ? "La fuente de pago se activó correctamente."
                    : "La fuente de pago se desactivó. Sus datos se conservan.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/fuente-pago/listado";
    }

    private List<FuentePago> cargarListado(Model model) {
        var fuentes = fuentePagoService.getFuentesPago(false);
        model.addAttribute("fuentesPago", fuentes);
        model.addAttribute("totalFuentesPago", fuentes.size());
        return fuentes;
    }
}
