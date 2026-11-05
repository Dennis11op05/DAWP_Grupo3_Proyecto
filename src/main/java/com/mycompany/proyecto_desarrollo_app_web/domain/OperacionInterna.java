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
@Table(name = "operacion_interna", uniqueConstraints = @UniqueConstraint(columnNames = {"id_fuente_pago", "referencia", "tipo"}))
public class OperacionInterna implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_operacion_interna")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_lote_importacion")
    private LoteImportacion lote;
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_fuente_pago")
    private FuentePago fuentePago;
    @Column(name = "referencia_liquidacion", nullable = false, length = 80)
    private String referenciaLiquidacion;
    @Column(nullable = false, length = 80)
    private String referencia;
    @Column(nullable = false, length = 20)
    private String tipo;
    @Column(nullable = false)
    private LocalDate fecha;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal importe;
    @Column(nullable = false, length = 3)
    private String moneda;
    @Column(name = "referencia_original", length = 80)
    private String referenciaOriginal;
    public BigDecimal getImporteNeto() {
        return "COBRO".equals(tipo) ? importe : importe.negate();
    }
}