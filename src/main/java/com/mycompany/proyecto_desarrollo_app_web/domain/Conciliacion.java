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
@Table(name = "conciliacion")
public class Conciliacion implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conciliacion")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_liquidacion", unique = true)
    private Liquidacion liquidacion;
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_creador")
    private Usuario creador;
    @ManyToOne
    @JoinColumn(name = "id_autorizante")
    private Usuario autorizante;
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;
    @Column(nullable = false, length = 20)
    private String estado = "ABIERTA";
    @Column(name = "total_calculado", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCalculado = BigDecimal.ZERO;
    @Column(name = "total_interno", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalInterno = BigDecimal.ZERO;
    @Column(name = "diferencia_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal diferenciaTotal = BigDecimal.ZERO;
}