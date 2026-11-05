package com.mycompany.proyecto_desarrollo_app_web.controller;

import com.mycompany.proyecto_desarrollo_app_web.service.*;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/importacion")
@RequiredArgsConstructor
public class ImportacionController {
    private final ImportacionService importaciones;
    private final FuentePagoService fuentes;
    @GetMapping("/listado")
    public String listado(Model model) {
        model.addAttribute("lotes", importaciones.historial());
        model.addAttribute("fuentes", fuentes.getFuentesPago(true));
        return "importacion/listado";
    }
    @PostMapping("/guardar")
    public String guardar(@RequestParam Integer fuenteId, @RequestParam String origen,
            @RequestParam MultipartFile archivo, RedirectAttributes flash) {
        var lote = importaciones.importar(fuenteId, origen, archivo);
        flash.addFlashAttribute(lote.getEstado().equals("VALIDO") ? "todoOk" : "error", lote.getResultado());
        return "redirect:/importacion/listado";
    }
    @GetMapping("/ejemplo/{origen}")
    public ResponseEntity<Resource> ejemplo(@PathVariable String origen) {
        if (!Set.of("interno", "proveedor", "invalido").contains(origen)) throw new IllegalArgumentException("Ejemplo no disponible.");
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + origen + ".csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(new ClassPathResource("ejemplos/" + origen + ".csv"));
    }
}
