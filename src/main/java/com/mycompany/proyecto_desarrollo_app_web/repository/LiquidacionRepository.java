package com.mycompany.proyecto_desarrollo_app_web.repository;

import com.mycompany.proyecto_desarrollo_app_web.domain.Liquidacion;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LiquidacionRepository extends JpaRepository<Liquidacion, Long> {
    List<Liquidacion> findAllByOrderByFechaDescIdDesc();
    boolean existsByFuentePago_IdFuentePagoAndReferencia(Integer fuente, String referencia);
    Optional<Liquidacion> findByFuentePago_IdFuentePagoAndReferencia(Integer fuente, String referencia);
}