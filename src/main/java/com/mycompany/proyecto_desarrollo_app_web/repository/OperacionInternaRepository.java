package com.mycompany.proyecto_desarrollo_app_web.repository;

import com.mycompany.proyecto_desarrollo_app_web.domain.OperacionInterna;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OperacionInternaRepository extends JpaRepository<OperacionInterna, Long> {
    List<OperacionInterna> findByFuentePago_IdFuentePagoAndReferenciaLiquidacionOrderByIdAsc(Integer fuente, String liquidacion);
    boolean existsByFuentePago_IdFuentePagoAndReferenciaAndTipo(Integer fuente, String referencia, String tipo);
}