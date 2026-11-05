package com.mycompany.proyecto_desarrollo_app_web.domain;

import jakarta.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioFormulario {
    private Long id;
    @NotBlank @Size(max = 100) private String nombre;
    @NotBlank @Email @Size(max = 150) private String correo;
    @Size(max = 64) private String password = "";
    private boolean activo = true;
    @NotEmpty(message = "Seleccione al menos un rol.")
    private Set<Long> roles = new HashSet<>();
}
