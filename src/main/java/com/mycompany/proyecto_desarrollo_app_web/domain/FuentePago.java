package com.mycompany.proyecto_desarrollo_app_web.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Locale;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fuente_pago")
public class FuentePago implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fuente_pago")
    private Integer idFuentePago;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El código es obligatorio.")
    @Size(max = 30, message = "El código no puede superar 30 caracteres.")
    @Pattern(regexp = "[A-Z0-9_-]+", message = "Use letras, números, guiones o guiones bajos en el código.")
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @NotBlank(message = "La moneda es obligatoria.")
    @Pattern(regexp = "CRC|USD|EUR", message = "Seleccione CRC, USD o EUR.")
    @Column(nullable = false, length = 3)
    private String moneda = "CRC";

    @Column(nullable = false)
    private boolean activo = true;

    public void setNombre(String nombre) {
        this.nombre = nombre == null ? null : nombre.trim();
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo == null ? null : codigo.trim().toUpperCase(Locale.ROOT);
    }
}
