package com.mycompany.proyecto_desarrollo_app_web.service;

import com.mycompany.proyecto_desarrollo_app_web.domain.*;
import com.mycompany.proyecto_desarrollo_app_web.repository.*;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<Usuario> listar() { return usuarios.findAllByOrderByNombreAsc(); }
    @Transactional(readOnly = true)
    public List<Rol> roles() { return roles.findAll(); }

    @Transactional(readOnly = true)
    public UsuarioFormulario formulario(Long id) {
        Usuario u = usuarios.findById(id).orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        UsuarioFormulario f = new UsuarioFormulario();
        f.setId(u.getId()); f.setNombre(u.getNombre()); f.setCorreo(u.getCorreo()); f.setActivo(u.isActivo());
        u.getRoles().forEach(r -> f.getRoles().add(r.getId()));
        return f;
    }

    @Transactional
    public void guardar(UsuarioFormulario f) {
        // Serializa cambios administrativos para no desactivar simultáneamente a los últimos administradores.
        usuarios.bloquearTodos();
        String correo = f.getCorreo().trim().toLowerCase(Locale.ROOT);
        var repetido = usuarios.findByCorreoIgnoreCase(correo);
        if (repetido.isPresent() && !repetido.get().getId().equals(f.getId()))
            throw new IllegalArgumentException("El correo ya está registrado.");
        var seleccion = new HashSet<>(roles.findAllById(f.getRoles()));
        if (seleccion.isEmpty() || seleccion.size() != f.getRoles().size())
            throw new IllegalArgumentException("Seleccione roles válidos.");
        Usuario u = f.getId() == null ? new Usuario() : usuarios.findById(f.getId())
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        if ((u.getId() == null || !f.getPassword().isEmpty()) && f.getPassword().length() < 10)
            throw new IllegalArgumentException("La contraseña debe tener entre 10 y 64 caracteres.");
        if (u.getId() != null && u.isActivo() && u.getRoles().stream().anyMatch(r -> r.getCodigo().equals("ADMIN"))
                && (!f.isActivo() || seleccion.stream().noneMatch(r -> r.getCodigo().equals("ADMIN")))) {
            boolean otro = usuarios.findByActivoTrue().stream().anyMatch(a -> !a.getId().equals(u.getId())
                    && a.getRoles().stream().anyMatch(r -> r.getCodigo().equals("ADMIN")));
            if (!otro) throw new IllegalArgumentException("Debe conservar al menos un administrador activo.");
        }
        u.setNombre(f.getNombre().trim()); u.setCorreo(correo); u.setActivo(f.isActivo()); u.setRoles(seleccion);
        if (!f.getPassword().isEmpty()) u.setPasswordHash(encoder.encode(f.getPassword()));
        usuarios.saveAndFlush(u);
    }
}
