package com.mycompany.proyecto_desarrollo_app_web.service;

import com.mycompany.proyecto_desarrollo_app_web.domain.FuentePago;
import com.mycompany.proyecto_desarrollo_app_web.repository.FuentePagoRepository;
import com.mycompany.proyecto_desarrollo_app_web.repository.LoteImportacionRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FuentePagoService {

    private final FuentePagoRepository fuentePagoRepository;
    private final LoteImportacionRepository lotes;

    @Transactional(readOnly = true)
    public List<FuentePago> getFuentesPago(boolean soloActivas) {
        return soloActivas ? fuentePagoRepository.findByActivoTrueOrderByNombreAsc()
                : fuentePagoRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Optional<FuentePago> getFuentePago(Integer idFuentePago) {
        return fuentePagoRepository.findById(idFuentePago);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void save(FuentePago fuentePago) {
        var existente = fuentePagoRepository.findByCodigo(fuentePago.getCodigo());
        if (existente.isPresent()
                && !existente.get().getIdFuentePago().equals(fuentePago.getIdFuentePago())) {
            throw new IllegalArgumentException("Ya existe una fuente de pago con ese código.");
        }

        // Con identificador se modifica un registro existente; sin él se crea uno nuevo.
        FuentePago destino = fuentePago.getIdFuentePago() == null ? new FuentePago()
                : fuentePagoRepository.bloquear(fuentePago.getIdFuentePago())
                        .orElseThrow(() -> new IllegalArgumentException("La fuente de pago no existe."));
        if (destino.getIdFuentePago() != null && lotes.existsByFuentePago_IdFuentePago(destino.getIdFuentePago())
                && !destino.getMoneda().equals(fuentePago.getMoneda()))
            throw new IllegalArgumentException("No puede cambiar la moneda de una fuente con importaciones.");
        destino.setNombre(fuentePago.getNombre());
        destino.setCodigo(fuentePago.getCodigo());
        destino.setMoneda(fuentePago.getMoneda());
        destino.setActivo(fuentePago.isActivo());
        fuentePagoRepository.saveAndFlush(destino);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void cambiarEstado(Integer idFuentePago, boolean activo) {
        FuentePago fuente = fuentePagoRepository.bloquear(idFuentePago)
                .orElseThrow(() -> new IllegalArgumentException("La fuente de pago no existe."));
        fuente.setActivo(activo);
        fuentePagoRepository.save(fuente);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Integer idFuentePago) {
        if (!fuentePagoRepository.existsById(idFuentePago)) {
            throw new IllegalArgumentException("La fuente de pago no existe.");
        }
        try {
            fuentePagoRepository.deleteById(idFuentePago);
            fuentePagoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "La fuente tiene datos relacionados. Desactívela para conservar el historial.", e);
        }
    }
}
