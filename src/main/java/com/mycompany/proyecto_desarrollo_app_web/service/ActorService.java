package com.mycompany.proyecto_desarrollo_app_web.service;

import com.mycompany.proyecto_desarrollo_app_web.domain.Usuario;
import com.mycompany.proyecto_desarrollo_app_web.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActorService {
    private final UsuarioRepository usuarios;
    public Usuario actual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new IllegalArgumentException("Debe iniciar sesión.");
        return usuarios.findByCorreoIgnoreCase(auth.getName()).filter(Usuario::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta no está disponible."));
    }
}
