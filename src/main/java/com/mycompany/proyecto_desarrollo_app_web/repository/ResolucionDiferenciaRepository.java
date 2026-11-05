package com.mycompany.proyecto_desarrollo_app_web.repository;

import com.mycompany.proyecto_desarrollo_app_web.domain.ResolucionDiferencia;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResolucionDiferenciaRepository extends JpaRepository<ResolucionDiferencia, Long> {
    List<ResolucionDiferencia> findByDiferencia_IdOrderByFechaAscIdAsc(Long diferencia);
    boolean existsByDiferencia_Conciliacion_Id(Long conciliacion);
    boolean existsByDiferencia_IdAndEstado(Long diferencia, String estado);
}