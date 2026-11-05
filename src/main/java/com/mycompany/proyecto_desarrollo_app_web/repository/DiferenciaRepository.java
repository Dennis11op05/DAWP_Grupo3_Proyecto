package com.mycompany.proyecto_desarrollo_app_web.repository;

import com.mycompany.proyecto_desarrollo_app_web.domain.Diferencia;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiferenciaRepository extends JpaRepository<Diferencia, Long> {
    List<Diferencia> findByConciliacion_IdOrderByIdAsc(Long conciliacion);
    List<Diferencia> findAllByOrderByIdDesc();
    long countByEstadoNot(String estado);
    boolean existsByConciliacion_IdAndEstadoNot(Long conciliacion, String estado);
}