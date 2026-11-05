package com.mycompany.proyecto_desarrollo_app_web.controller;

import com.mycompany.proyecto_desarrollo_app_web.service.*;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/liquidacion")
@RequiredArgsConstructor
public class LiquidacionController {
    private final LiquidacionService liquidaciones;
    private final ConciliacionService conciliaciones;
    @GetMapping("/listado")
    public String listado(Model model) {
        model.addAttribute("liquidaciones", liquidaciones.listar());
        model.addAttribute("conciliadas", liquidaciones.conciliadas());
        return "liquidacion/listado";
    }
    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        var liquidacion = liquidaciones.obtener(id);
        var movimientos = conciliaciones.movimientos(id);
        model.addAttribute("liquidacion", liquidacion);
        model.addAttribute("movimientos", movimientos);
        model.addAttribute("neto", movimientos.stream().map(m -> m.getImporteNeto()).reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("conciliacionId", liquidaciones.conciliadas().get(id));
        return "liquidacion/detalle";
    }
}
