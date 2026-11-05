package com.mycompany.proyecto_desarrollo_app_web.controller;

import com.mycompany.proyecto_desarrollo_app_web.service.ConciliacionService;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ConciliacionController {
    private final ConciliacionService conciliaciones;

    @GetMapping({"/conciliacion/listado", "/reporte/conciliaciones"})
    public String listado(@RequestParam(defaultValue = "") String referencia,
            @RequestParam(defaultValue = "") String estado, Model model) {
        model.addAttribute("conciliaciones", conciliaciones.listar(referencia, estado));
        model.addAttribute("referencia", referencia); model.addAttribute("estado", estado);
        return "conciliacion/listado";
    }
    @PostMapping("/conciliacion/iniciar")
    public String iniciar(@RequestParam Long liquidacionId) {
        return "redirect:/conciliacion/detalle/" + conciliaciones.iniciar(liquidacionId).getId();
    }
    @GetMapping("/conciliacion/detalle/{id}")
    public String detalle(@PathVariable Long id, @RequestParam(defaultValue = "") String buscar, Model model) {
        var c = conciliaciones.obtener(id);
        var enlaces = conciliaciones.vinculos(id);
        var internas = conciliaciones.internas(c);
        model.addAttribute("conciliacion", c);
        model.addAttribute("movimientos", conciliaciones.movimientos(c.getLiquidacion().getId()));
        model.addAttribute("operaciones", internas.stream().filter(o -> o.getReferencia().contains(buscar.trim().toUpperCase(java.util.Locale.ROOT))).toList());
        model.addAttribute("referencias", internas);
        model.addAttribute("buscar", buscar);
        model.addAttribute("vinculos", enlaces);
        model.addAttribute("movimientosVinculados", enlaces.stream().map(v -> v.getMovimiento().getId()).collect(Collectors.toSet()));
        var diferencias = conciliaciones.diferencias(id);
        model.addAttribute("diferencias", diferencias);
        model.addAttribute("pendientes", diferencias.stream().filter(d -> !d.getEstado().equals("RESUELTA")).count());
        model.addAttribute("vinculosProtegidos", conciliaciones.vinculosProtegidos(id));
        return "conciliacion/detalle";
    }
    @PostMapping("/conciliacion/vincular")
    public String vincular(@RequestParam Long id, @RequestParam Long movimientoId, @RequestParam String referencia, RedirectAttributes flash) {
        conciliaciones.vincular(id, movimientoId, referencia);
        flash.addFlashAttribute("todoOk", "Vínculo registrado y diferencias recalculadas.");
        return "redirect:/conciliacion/detalle/" + id;
    }
    @PostMapping("/conciliacion/cerrar")
    public String cerrar(@RequestParam Long id, RedirectAttributes flash) {
        conciliaciones.cerrar(id); flash.addFlashAttribute("todoOk", "Conciliación cerrada. Sus datos quedan disponibles para consulta.");
        return "redirect:/conciliacion/detalle/" + id;
    }
    @GetMapping({"/diferencia/listado", "/reporte/diferencias"})
    public String diferencias(Model model) {
        model.addAttribute("diferencias", conciliaciones.todasDiferencias());
        return "diferencia/listado";
    }
    @GetMapping("/diferencia/detalle/{id}")
    public String diferencia(@PathVariable Long id, Model model) {
        model.addAttribute("diferencia", conciliaciones.diferencia(id));
        model.addAttribute("resoluciones", conciliaciones.historial(id));
        return "diferencia/detalle";
    }
    @PostMapping("/diferencia/proponer")
    public String proponer(@RequestParam Long id, @RequestParam String clasificacion, @RequestParam String explicacion, RedirectAttributes flash) {
        conciliaciones.proponer(id, clasificacion, explicacion); flash.addFlashAttribute("todoOk", "Propuesta enviada al supervisor.");
        return "redirect:/diferencia/detalle/" + id;
    }
    @PostMapping("/diferencia/evaluar")
    public String evaluar(@RequestParam Long resolucionId, @RequestParam boolean aprobar,
            @RequestParam(defaultValue = "") String motivo, RedirectAttributes flash) {
        Long diferenciaId = conciliaciones.diferenciaDeResolucion(resolucionId);
        conciliaciones.evaluar(resolucionId, aprobar, motivo); flash.addFlashAttribute("todoOk", aprobar ? "Propuesta aprobada." : "Propuesta rechazada. El historial se conserva.");
        return "redirect:/diferencia/detalle/" + diferenciaId;
    }
}
