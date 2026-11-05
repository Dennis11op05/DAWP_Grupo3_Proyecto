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
@Table(name = "diferencia")
public class Diferencia implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_diferencia")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_conciliacion")
    private Conciliacion conciliacion;
    @ManyToOne
    @JoinColumn(name = "id_operacion_interna")
    private OperacionInterna operacion;
    @ManyToOne
    @JoinColumn(name = "id_movimiento_proveedor")
    private MovimientoProveedor movimiento;
    @Column(nullable = false, length = 30)
    private String tipo;
    @Column(nullable = false, length = 500)
    private String descripcion;
    @Column(nullable = false, length = 25)
    private String estado = "PENDIENTE";
    @Column(precision = 15, scale = 2)
    private BigDecimal importe;
}