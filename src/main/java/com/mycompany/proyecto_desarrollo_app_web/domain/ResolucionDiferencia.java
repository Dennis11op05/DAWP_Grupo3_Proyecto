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
@Table(name = "resolucion_diferencia")
public class ResolucionDiferencia implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resolucion_diferencia")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_diferencia")
    private Diferencia diferencia;
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_proponente")
    private Usuario proponente;
    @ManyToOne
    @JoinColumn(name = "id_evaluador")
    private Usuario evaluador;
    @Column(nullable = false, length = 30)
    private String clasificacion;
    @Column(nullable = false, length = 2000)
    private String explicacion;
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;
    @Column(nullable = false, length = 20)
    private String estado = "PROPUESTA";
    @Column(name = "motivo_rechazo", length = 2000)
    private String motivoRechazo;
}