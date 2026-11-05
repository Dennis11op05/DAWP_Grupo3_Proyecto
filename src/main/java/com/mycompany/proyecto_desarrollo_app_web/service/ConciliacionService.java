package com.mycompany.proyecto_desarrollo_app_web.service;

import com.mycompany.proyecto_desarrollo_app_web.domain.*;
import com.mycompany.proyecto_desarrollo_app_web.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ANALISTA','SUPERVISOR')")
public class ConciliacionService {
    @PersistenceContext private EntityManager entityManager;
    private final ConciliacionRepository conciliaciones;
    private final LiquidacionRepository liquidaciones;
    private final OperacionInternaRepository operaciones;
    private final MovimientoProveedorRepository movimientos;
    private final VinculoConciliacionRepository vinculos;
    private final DiferenciaRepository diferencias;
    private final ResolucionDiferenciaRepository resoluciones;
    private final FuentePagoRepository fuentes;
    private final ActorService actor;

    @Transactional(readOnly = true)
    public List<Conciliacion> listar(String referencia, String estado) {
        if (!Set.of("", "ABIERTA", "CERRADA").contains(estado)) throw new IllegalArgumentException("Estado de búsqueda inválido.");
        String buscar = referencia.trim().toUpperCase(Locale.ROOT);
        return conciliaciones.findAllByOrderByFechaDesc().stream()
                .filter(c -> c.getLiquidacion().getReferencia().contains(buscar))
                .filter(c -> estado.isEmpty() || estado.equals(c.getEstado())).toList();
    }

    @Transactional(readOnly = true)
    public Conciliacion obtener(Long id) {
        return conciliaciones.findById(id).orElseThrow(() -> new IllegalArgumentException("La conciliación no existe."));
    }

    @Transactional(readOnly = true)
    public List<OperacionInterna> internas(Conciliacion c) {
        return operaciones.findByFuentePago_IdFuentePagoAndReferenciaLiquidacionOrderByIdAsc(
                c.getLiquidacion().getFuentePago().getIdFuentePago(), c.getLiquidacion().getReferencia());
    }

    @Transactional(readOnly = true)
    public List<MovimientoProveedor> movimientos(Long liquidacion) { return movimientos.findByLiquidacion_IdOrderByIdAsc(liquidacion); }
    @Transactional(readOnly = true)
    public List<VinculoConciliacion> vinculos(Long id) { return vinculos.findByConciliacion_IdOrderByIdAsc(id); }
    @Transactional(readOnly = true)
    public List<Diferencia> diferencias(Long id) { return diferencias.findByConciliacion_IdOrderByIdAsc(id); }
    @Transactional(readOnly = true)
    public List<Diferencia> todasDiferencias() { return diferencias.findAllByOrderByIdDesc(); }

    @Transactional
    @PreAuthorize("hasRole('ANALISTA')")
    public Conciliacion iniciar(Long liquidacionId) {
        Liquidacion l = liquidaciones.findById(liquidacionId).orElseThrow(() -> new IllegalArgumentException("La liquidación no existe."));
        fuentes.bloquear(l.getFuentePago().getIdFuentePago()).orElseThrow();
        if (conciliaciones.existsByLiquidacion_Id(l.getId())) throw new IllegalArgumentException("La liquidación ya tiene una conciliación.");
        Conciliacion c = new Conciliacion(); c.setLiquidacion(l); c.setCreador(actor.actual());
        conciliaciones.saveAndFlush(c);
        actualizarDiferencias(c);
        return c;
    }

    @Transactional
    @PreAuthorize("hasRole('ANALISTA')")
    public void vincular(Long id, Long movimientoId, String referencia) {
        Conciliacion c = abierta(id);
        if (resoluciones.existsByDiferencia_Conciliacion_Id(id))
            throw new IllegalArgumentException("Los vínculos quedan protegidos al proponer la primera resolución. Complete las vinculaciones antes de resolver diferencias.");
        MovimientoProveedor m = movimientos.findById(movimientoId).orElseThrow(() -> new IllegalArgumentException("El movimiento no existe."));
        if (!m.getLiquidacion().getId().equals(c.getLiquidacion().getId()))
            throw new IllegalArgumentException("El movimiento pertenece a otra liquidación.");
        String ref = referencia.trim().toUpperCase(Locale.ROOT);
        OperacionInterna o = internas(c).stream().filter(i -> i.getReferencia().equals(ref) && i.getTipo().equals(m.getTipo()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("No existe una operación interna con esa referencia y tipo en esta liquidación."));
        if (!o.getReferencia().equals(m.getReferencia()) || !o.getMoneda().equals(m.getMoneda()))
            throw new IllegalArgumentException("La referencia y moneda deben coincidir. Las diferencias de importe sí pueden investigarse.");
        if (vinculos.existsByOperacion_Id(o.getId()) || vinculos.existsByMovimiento_Id(m.getId()))
            throw new IllegalArgumentException("Uno de los registros ya está vinculado.");
        VinculoConciliacion v = new VinculoConciliacion(); v.setConciliacion(c); v.setOperacion(o); v.setMovimiento(m); v.setUsuario(actor.actual());
        vinculos.saveAndFlush(v);
        actualizarDiferencias(c);
    }

    @Transactional
    @PreAuthorize("hasRole('ANALISTA')")
    public void proponer(Long diferenciaId, String clasificacion, String explicacion) {
        Diferencia d = diferencias.findById(diferenciaId).orElseThrow(() -> new IllegalArgumentException("La diferencia no existe."));
        abierta(d.getConciliacion().getId());
        entityManager.refresh(d);
        if (!d.getEstado().equals("PENDIENTE") || resoluciones.existsByDiferencia_IdAndEstado(diferenciaId, "PROPUESTA"))
            throw new IllegalArgumentException("La diferencia ya está resuelta o tiene una propuesta pendiente.");
        if (!Set.of("COMISION", "DEVOLUCION", "REGISTRO_FALTANTE", "AJUSTE_JUSTIFICADO", "OTRO").contains(clasificacion))
            throw new IllegalArgumentException("Clasificación inválida.");
        texto(explicacion, "La explicación es obligatoria y admite hasta 2000 caracteres.");
        ResolucionDiferencia r = new ResolucionDiferencia(); r.setDiferencia(d); r.setClasificacion(clasificacion);
        r.setExplicacion(explicacion.trim()); r.setProponente(actor.actual()); resoluciones.save(r);
        d.setEstado("EN_REVISION"); diferencias.save(d);
    }

    @Transactional
    @PreAuthorize("hasRole('SUPERVISOR')")
    public void evaluar(Long resolucionId, boolean aprobar, String motivo) {
        ResolucionDiferencia r = resoluciones.findById(resolucionId).orElseThrow(() -> new IllegalArgumentException("La propuesta no existe."));
        abierta(r.getDiferencia().getConciliacion().getId());
        entityManager.refresh(r);
        if (!r.getEstado().equals("PROPUESTA")) throw new IllegalArgumentException("La propuesta ya fue evaluada.");
        Usuario evaluador = actor.actual();
        if (evaluador.getId().equals(r.getProponente().getId())) throw new IllegalArgumentException("No puede evaluar su propia propuesta.");
        if (!aprobar) texto(motivo, "Indique el motivo del rechazo (hasta 2000 caracteres).");
        r.setEvaluador(evaluador); r.setFechaRevision(LocalDateTime.now());
        r.setEstado(aprobar ? "APROBADA" : "RECHAZADA"); r.setMotivoRechazo(aprobar ? null : motivo.trim());
        r.getDiferencia().setEstado(aprobar ? "RESUELTA" : "PENDIENTE");
        resoluciones.save(r); diferencias.save(r.getDiferencia());
    }

    @Transactional
    @PreAuthorize("hasRole('SUPERVISOR')")
    public void cerrar(Long id) {
        Conciliacion c = abierta(id);
        if (diferencias.existsByConciliacion_IdAndEstadoNot(id, "RESUELTA"))
            throw new IllegalArgumentException("No puede cerrar una conciliación con diferencias pendientes o en revisión.");
        c.setEstado("CERRADA"); c.setAutorizante(actor.actual()); c.setFechaCierre(LocalDateTime.now()); conciliaciones.save(c);
    }

    @Transactional(readOnly = true)
    public Diferencia diferencia(Long id) {
        return diferencias.findById(id).orElseThrow(() -> new IllegalArgumentException("La diferencia no existe."));
    }
    @Transactional(readOnly = true)
    public List<ResolucionDiferencia> historial(Long id) { return resoluciones.findByDiferencia_IdOrderByFechaAscIdAsc(id); }
    @Transactional(readOnly = true)
    public boolean vinculosProtegidos(Long id) { return resoluciones.existsByDiferencia_Conciliacion_Id(id); }
    @Transactional(readOnly = true)
    public Long diferenciaDeResolucion(Long id) {
        return resoluciones.findById(id).orElseThrow(() -> new IllegalArgumentException("La propuesta no existe.")).getDiferencia().getId();
    }

    private Conciliacion abierta(Long id) {
        Conciliacion c = conciliaciones.bloquear(id).orElseThrow(() -> new IllegalArgumentException("La conciliación no existe."));
        if (c.getEstado().equals("CERRADA")) throw new IllegalArgumentException("La conciliación está cerrada y solo admite consulta.");
        return c;
    }
    private void texto(String valor, String mensaje) {
        if (valor == null || valor.isBlank() || valor.length() > 2000) throw new IllegalArgumentException(mensaje);
    }

    private void actualizarDiferencias(Conciliacion c) {
        var internas = internas(c);
        var externas = movimientos(c.getLiquidacion().getId());
        var enlaces = vinculos(c.getId());
        // Solo se recalcula antes de que existan propuestas; nunca se borra un historial de resoluciones.
        diferencias.deleteAll(diferencias.findByConciliacion_IdOrderByIdAsc(c.getId()));
        diferencias.flush();
        Set<Long> internasUsadas = new HashSet<>(), externasUsadas = new HashSet<>();
        for (var v : enlaces) {
            internasUsadas.add(v.getOperacion().getId()); externasUsadas.add(v.getMovimiento().getId());
            BigDecimal diferencia = v.getOperacion().getImporteNeto().subtract(v.getMovimiento().getImporteNeto());
            if (diferencia.signum() != 0) registrar(c, "IMPORTE", "Importes diferentes para " + v.getOperacion().getReferencia(), diferencia, v.getOperacion(), v.getMovimiento());
            if (!Objects.equals(v.getOperacion().getReferenciaOriginal(), v.getMovimiento().getReferenciaOriginal()))
                registrar(c, "REFERENCIA_ORIGINAL", "Referencias originales distintas para " + v.getOperacion().getReferencia(), null, v.getOperacion(), v.getMovimiento());
        }
        for (var o : internas) if (!internasUsadas.contains(o.getId()))
            registrar(c, "SIN_MOVIMIENTO", "Operación interna sin movimiento vinculado: " + o.getReferencia(), o.getImporteNeto(), o, null);
        for (var m : externas) if (!externasUsadas.contains(m.getId()))
            registrar(c, "SIN_OPERACION", "Movimiento sin operación interna vinculada: " + m.getReferencia(), m.getImporteNeto(), null, m);
        c.setTotalCalculado(externas.stream().map(MovimientoProveedor::getImporteNeto).reduce(BigDecimal.ZERO, BigDecimal::add));
        c.setTotalInterno(internas.stream().map(OperacionInterna::getImporteNeto).reduce(BigDecimal.ZERO, BigDecimal::add));
        c.setDiferenciaTotal(c.getTotalCalculado().subtract(c.getLiquidacion().getImporteReportado()));
        if (c.getDiferenciaTotal().signum() != 0)
            registrar(c, "TOTAL_LIQUIDACION", "El neto de movimientos no coincide con el total reportado.", c.getDiferenciaTotal(), null, null);
        conciliaciones.save(c);
    }
    private void registrar(Conciliacion c, String tipo, String descripcion, BigDecimal importe, OperacionInterna o, MovimientoProveedor m) {
        Diferencia d = new Diferencia(); d.setConciliacion(c); d.setTipo(tipo); d.setDescripcion(descripcion);
        d.setImporte(importe); d.setOperacion(o); d.setMovimiento(m); diferencias.save(d);
    }
}
