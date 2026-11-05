package com.mycompany.proyecto_desarrollo_app_web.service;

import com.mycompany.proyecto_desarrollo_app_web.domain.*;
import com.mycompany.proyecto_desarrollo_app_web.repository.*;
import java.security.MessageDigest;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImportacionService {
    private final FuentePagoRepository fuentes;
    private final LoteImportacionRepository lotes;
    private final ActorService actor;
    private final ImportacionPersistenciaService persistencia;

    @PreAuthorize("hasAnyRole('ANALISTA','ADMIN')")
    public List<LoteImportacion> historial() { return lotes.findAllByOrderByFechaDesc(); }

    @PreAuthorize("hasRole('ANALISTA')")
    public LoteImportacion importar(Integer fuenteId, String origen, MultipartFile archivo) {
        FuentePago fuente = fuentes.findById(fuenteId).orElseThrow(() -> new IllegalArgumentException("Seleccione una fuente válida."));
        if (!Set.of("INTERNO", "PROVEEDOR").contains(origen)) throw new IllegalArgumentException("Origen inválido.");
        byte[] bytes;
        try { bytes = archivo.getBytes(); } catch (java.io.IOException e) { throw new IllegalArgumentException("No se pudo leer el archivo."); }
        if (bytes.length > 5 * 1024 * 1024) throw new IllegalArgumentException("El máximo es de 5 MB.");
        LoteImportacion lote = new LoteImportacion();
        lote.setFuentePago(fuente); lote.setUsuario(actor.actual()); lote.setOrigen(origen);
        String nombre = Objects.toString(archivo.getOriginalFilename(), "archivo.csv").replace('\\', '/');
        nombre = nombre.substring(nombre.lastIndexOf('/') + 1);
        lote.setArchivo(nombre.substring(0, Math.min(nombre.length(), 200)));
        try { lote.setHuella(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes))); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException("No se pudo verificar el archivo."); }
        try {
            if (!nombre.toLowerCase(Locale.ROOT).endsWith(".csv") || bytes.length == 0)
                throw new IllegalArgumentException("Seleccione un archivo CSV no vacío.");
            return persistencia.guardar(lote, ArchivoCsv.leer(bytes));
        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            // La transacción de persistencia ya terminó en rollback. Se conserva solo el lote rechazado.
            lote.setId(null); lote.setEstado("ERROR"); lote.setCantidad(0);
            String mensaje = e instanceof IllegalArgumentException ? e.getMessage() : "Datos duplicados o incompatibles. No se importó ningún registro.";
            lote.setResultado(mensaje.substring(0, Math.min(mensaje.length(), 2000)));
            return lotes.save(lote);
        }
    }
}
