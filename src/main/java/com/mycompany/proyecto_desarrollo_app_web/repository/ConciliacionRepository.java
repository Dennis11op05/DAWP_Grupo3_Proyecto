package com.mycompany.proyecto_desarrollo_app_web.repository;

import com.mycompany.proyecto_desarrollo_app_web.domain.Conciliacion;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConciliacionRepository extends JpaRepository<Conciliacion, Long> {
    Optional<Conciliacion> findByLiquidacion_Id(Long liquidacion);
    boolean existsByLiquidacion_Id(Long liquidacion);
    List<Conciliacion> findAllByOrderByFechaDesc();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conciliacion c where c.id = :id")
    Optional<Conciliacion> bloquear(@Param("id") Long id);
}