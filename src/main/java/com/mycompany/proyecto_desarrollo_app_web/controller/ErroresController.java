package com.mycompany.proyecto_desarrollo_app_web.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class ErroresController {
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, DataIntegrityViolationException.class,
        MaxUploadSizeExceededException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String negocio(Exception error, Model model) {
        String mensaje = error instanceof MaxUploadSizeExceededException ? "El CSV no debe superar 5 MB."
                : error instanceof DataIntegrityViolationException ? "La operación entra en conflicto con datos existentes. Actualice la página y revise los registros."
                : error.getMessage();
        model.addAttribute("mensaje", mensaje);
        return "error/negocio";
    }
}
