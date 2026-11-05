package com.mycompany.proyecto_desarrollo_app_web.repository;

import com.mycompany.proyecto_desarrollo_app_web.domain.FuentePago;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface FuentePagoRepository extends JpaRepository<FuentePago, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from FuentePago f where f.idFuentePago = :id")
    Optional<FuentePago> bloquear(@Param("id") Integer id);

    List<FuentePago> findAllByOrderByNombreAsc();

    List<FuentePago> findByActivoTrueOrderByNombreAsc();

    Optional<FuentePago> findByCodigo(String codigo);
}
