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
@Table(name = "liquidacion", uniqueConstraints = @UniqueConstraint(columnNames = {"id_fuente_pago", "referencia"}))
public class Liquidacion implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_liquidacion")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_lote_importacion")
    private LoteImportacion lote;
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_fuente_pago")
    private FuentePago fuentePago;
    @Column(nullable = false, length = 80)
    private String referencia;
    @Column(nullable = false)
    private LocalDate fecha;
    @Column(name = "importe_reportado", nullable = false, precision = 15, scale = 2)
    private BigDecimal importeReportado;
    @Column(nullable = false, length = 3)
    private String moneda;
}