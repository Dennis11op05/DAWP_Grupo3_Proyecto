package com.mycompany.proyecto_desarrollo_app_web;

import com.mycompany.proyecto_desarrollo_app_web.domain.*;
import com.mycompany.proyecto_desarrollo_app_web.repository.*;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class Inicializador implements ApplicationRunner {
    private final RolRepository roles;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    @Value("${nexo.admin.correo:}") private String correo;
    @Value("${nexo.admin.password:}") private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String codigo : new String[]{"ADMIN", "ANALISTA", "SUPERVISOR"}) {
            if (roles.findByCodigo(codigo).isEmpty()) {
                Rol rol = new Rol(); rol.setCodigo(codigo); rol.setDescripcion(codigo);
                roles.save(rol);
            }
        }
        if (usuarios.count() == 0) {
            if (!correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+") || password.length() < 10 || password.length() > 64) {
                throw new IllegalStateException("Configure NEXO_ADMIN_EMAIL y NEXO_ADMIN_PASSWORD (10 a 64 caracteres) para crear el primer administrador.");
            }
            Usuario admin = new Usuario();
            admin.setNombre("Administrador inicial");
            admin.setCorreo(correo.trim().toLowerCase(Locale.ROOT));
            admin.setPasswordHash(encoder.encode(password));
            admin.setRoles(Set.of(roles.findByCodigo("ADMIN").orElseThrow()));
            usuarios.save(admin);
        }
    }
}
