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
@Table(name = "vinculo_conciliacion")
public class VinculoConciliacion implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vinculo_conciliacion")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_conciliacion")
    private Conciliacion conciliacion;
    @OneToOne(optional = false)
    @JoinColumn(name = "id_operacion_interna", unique = true)
    private OperacionInterna operacion;
    @OneToOne(optional = false)
    @JoinColumn(name = "id_movimiento_proveedor", unique = true)
    private MovimientoProveedor movimiento;
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
    @Column(nullable = false, length = 50)
    private String criterio = "REFERENCIA_Y_TIPO";
}