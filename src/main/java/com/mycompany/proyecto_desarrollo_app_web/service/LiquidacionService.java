package com.mycompany.proyecto_desarrollo_app_web.service;

import com.mycompany.proyecto_desarrollo_app_web.domain.*;
import com.mycompany.proyecto_desarrollo_app_web.repository.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ANALISTA','SUPERVISOR')")
public class LiquidacionService {
    private final LiquidacionRepository liquidaciones;
    private final ConciliacionRepository conciliaciones;
    @Transactional(readOnly = true)
    public List<Liquidacion> listar() { return liquidaciones.findAllByOrderByFechaDescIdDesc(); }
    @Transactional(readOnly = true)
    public Liquidacion obtener(Long id) {
        return liquidaciones.findById(id).orElseThrow(() -> new IllegalArgumentException("La liquidación no existe."));
    }
    @Transactional(readOnly = true)
    public Map<Long, Long> conciliadas() {
        Map<Long, Long> mapa = new HashMap<>();
        conciliaciones.findAll().forEach(c -> mapa.put(c.getLiquidacion().getId(), c.getId()));
        return mapa;
    }
}
