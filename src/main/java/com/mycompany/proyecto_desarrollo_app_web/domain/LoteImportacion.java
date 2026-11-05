package com.mycompany.proyecto_desarrollo_app_web.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "lote_importacion")
public class LoteImportacion implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote_importacion")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_fuente_pago")
    private FuentePago fuentePago;
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
    @Column(nullable = false, length = 20)
    private String origen;
    @Column(nullable = false, length = 200)
    private String archivo;
    @Column(nullable = false, length = 64)
    private String huella;
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
    @Column(nullable = false, length = 20)
    private String estado;
    @Column(nullable = false)
    private int cantidad;
    @Column(nullable = false, length = 2000)
    private String resultado;
}