package com.mycompany.proyecto_desarrollo_app_web.repository;

import com.mycompany.proyecto_desarrollo_app_web.domain.Usuario;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u order by u.id")
    List<Usuario> bloquearTodos();
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    List<Usuario> findAllByOrderByNombreAsc();
    List<Usuario> findByActivoTrue();
}
