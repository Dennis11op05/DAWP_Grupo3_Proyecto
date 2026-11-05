package com.mycompany.proyecto_desarrollo_app_web.service;

import com.mycompany.proyecto_desarrollo_app_web.domain.*;
import com.mycompany.proyecto_desarrollo_app_web.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImportacionPersistenciaService {
    private final FuentePagoRepository fuentes;
    private final LoteImportacionRepository lotes;
    private final OperacionInternaRepository operaciones;
    private final LiquidacionRepository liquidaciones;
    private final MovimientoProveedorRepository movimientos;
    private final ConciliacionRepository conciliaciones;

    @Transactional
    @PreAuthorize("hasRole('ANALISTA')")
    public LoteImportacion guardar(LoteImportacion lote, List<List<String>> filas) {
        FuentePago fuente = fuentes.bloquear(lote.getFuentePago().getIdFuentePago())
                .orElseThrow(() -> new IllegalArgumentException("La fuente no existe."));
        if (!fuente.isActivo()) throw new IllegalArgumentException("No se puede importar desde una fuente inactiva.");
        lote.setFuentePago(fuente);
        if (lotes.existsByFuentePago_IdFuentePagoAndOrigenAndHuellaAndEstado(fuente.getIdFuentePago(), lote.getOrigen(), lote.getHuella(), "VALIDO"))
            throw new IllegalArgumentException("Este archivo ya fue importado correctamente.");
        boolean interno = lote.getOrigen().equals("INTERNO");
        List<String> esperado = interno ? List.of("liquidacion", "referencia", "tipo", "fecha", "importe", "moneda", "referencia_original")
                : List.of("liquidacion", "fecha_liquidacion", "total_reportado", "referencia", "tipo", "fecha", "importe", "moneda", "referencia_original");
        if (!filas.getFirst().equals(esperado)) throw new IllegalArgumentException("Encabezados incorrectos. Descargue el ejemplo del origen seleccionado.");
        Set<String> claves = new HashSet<>();
        Map<String, Liquidacion> grupos = new LinkedHashMap<>();
        List<OperacionInterna> internas = new ArrayList<>();
        List<MovimientoProveedor> externas = new ArrayList<>();
        for (int i = 1; i < filas.size(); i++) {
            try {
                List<String> f = filas.get(i);
                if (f.size() != esperado.size()) throw new IllegalArgumentException("Cantidad de columnas incorrecta.");
                int offset = interno ? 0 : 2;
                String refLiquidacion = referencia(f.get(0));
                String referencia = referencia(f.get(1 + offset));
                String tipo = f.get(2 + offset).toUpperCase(Locale.ROOT);
                if (!Set.of("COBRO", "DEVOLUCION", "COMISION").contains(tipo)) throw new IllegalArgumentException("Tipo inválido.");
                LocalDate fecha = fecha(f.get(3 + offset));
                BigDecimal importe = dinero(f.get(4 + offset), false);
                String moneda = f.get(5 + offset).toUpperCase(Locale.ROOT);
                if (!moneda.equals(fuente.getMoneda())) throw new IllegalArgumentException("La moneda no coincide con la fuente.");
                String original = f.get(6 + offset).isBlank() ? null : referencia(f.get(6 + offset));
                if (tipo.equals("DEVOLUCION") && original == null) throw new IllegalArgumentException("La devolución requiere referencia_original.");
                if (!claves.add(referencia + "|" + tipo)) throw new IllegalArgumentException("Referencia y tipo repetidos dentro del archivo.");
                var liquidacionExistente = liquidaciones.findByFuentePago_IdFuentePagoAndReferencia(fuente.getIdFuentePago(), refLiquidacion);
                if (liquidacionExistente.isPresent() && conciliaciones.existsByLiquidacion_Id(liquidacionExistente.get().getId()))
                    throw new IllegalArgumentException("La liquidación ya tiene una conciliación; sus datos de origen están protegidos.");
                if (interno) {
                    if (operaciones.existsByFuentePago_IdFuentePagoAndReferenciaAndTipo(fuente.getIdFuentePago(), referencia, tipo))
                        throw new IllegalArgumentException("La operación ya existe.");
                    OperacionInterna o = new OperacionInterna();
                    o.setLote(lote); o.setFuentePago(fuente); o.setReferenciaLiquidacion(refLiquidacion);
                    o.setReferencia(referencia); o.setTipo(tipo); o.setFecha(fecha); o.setImporte(importe);
                    o.setMoneda(moneda); o.setReferenciaOriginal(original); internas.add(o);
                } else {
                    if (movimientos.existsByFuentePago_IdFuentePagoAndReferenciaAndTipo(fuente.getIdFuentePago(), referencia, tipo))
                        throw new IllegalArgumentException("El movimiento ya existe.");
                    LocalDate fechaLiquidacion = fecha(f.get(1));
                    BigDecimal total = dinero(f.get(2), true);
                    Liquidacion l = grupos.get(refLiquidacion);
                    if (l == null) {
                        if (liquidacionExistente.isPresent()) throw new IllegalArgumentException("La liquidación ya fue importada. Importe cada liquidación completa una sola vez.");
                        l = new Liquidacion(); l.setLote(lote); l.setFuentePago(fuente); l.setReferencia(refLiquidacion);
                        l.setFecha(fechaLiquidacion); l.setImporteReportado(total); l.setMoneda(moneda); grupos.put(refLiquidacion, l);
                    } else if (!l.getFecha().equals(fechaLiquidacion) || l.getImporteReportado().compareTo(total) != 0)
                        throw new IllegalArgumentException("Una liquidación tiene fechas o totales contradictorios.");
                    MovimientoProveedor m = new MovimientoProveedor();
                    m.setLote(lote); m.setFuentePago(fuente); m.setLiquidacion(l); m.setReferencia(referencia);
                    m.setTipo(tipo); m.setFecha(fecha); m.setImporte(importe); m.setMoneda(moneda); m.setReferenciaOriginal(original); externas.add(m);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Registro " + i + ": " + e.getMessage());
            }
        }
        lote.setEstado("VALIDO"); lote.setCantidad(filas.size() - 1); lote.setResultado("Validación correcta. Todos los registros se guardaron.");
        lotes.save(lote);
        if (interno) operaciones.saveAll(internas);
        else { liquidaciones.saveAll(grupos.values()); movimientos.saveAll(externas); }
        lotes.flush();
        return lote;
    }

    private String referencia(String valor) {
        if (!valor.matches("[A-Za-z0-9_.:/-]{1,80}")) throw new IllegalArgumentException("Referencia obligatoria de hasta 80 caracteres, sin espacios.");
        return valor.toUpperCase(Locale.ROOT);
    }
    private LocalDate fecha(String valor) {
        try { return LocalDate.parse(valor); }
        catch (RuntimeException e) { throw new IllegalArgumentException("Fecha inválida. Utilice AAAA-MM-DD."); }
    }
    private BigDecimal dinero(String valor, boolean negativo) {
        if (!valor.matches((negativo ? "-?" : "") + "[0-9]{1,13}(\\.[0-9]{1,2})?"))
            throw new IllegalArgumentException("Importe inválido. Use punto decimal, hasta 2 decimales y valores positivos para los movimientos.");
        return new BigDecimal(valor).setScale(2);
    }
}
