package com.mycompany.proyecto_desarrollo_app_web.repository;

import com.mycompany.proyecto_desarrollo_app_web.domain.VinculoConciliacion;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VinculoConciliacionRepository extends JpaRepository<VinculoConciliacion, Long> {
    List<VinculoConciliacion> findByConciliacion_IdOrderByIdAsc(Long conciliacion);
    boolean existsByOperacion_Id(Long operacion);
    boolean existsByMovimiento_Id(Long movimiento);
}