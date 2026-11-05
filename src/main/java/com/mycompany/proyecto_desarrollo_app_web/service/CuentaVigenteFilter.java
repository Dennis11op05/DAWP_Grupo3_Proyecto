package com.mycompany.proyecto_desarrollo_app_web.service;

import com.mycompany.proyecto_desarrollo_app_web.repository.UsuarioRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class CuentaVigenteFilter extends OncePerRequestFilter {
    private final UsuarioRepository usuarios;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            var usuario = usuarios.findByCorreoIgnoreCase(auth.getName());
            var permisosSesion = auth.getAuthorities().stream().map(a -> a.getAuthority())
                    .filter(a -> a.startsWith("ROLE_")).collect(Collectors.toSet());
            boolean vigente = usuario.isPresent() && usuario.get().isActivo()
                    && permisosSesion.equals(usuario.get().getRoles().stream()
                            .map(r -> "ROLE_" + r.getCodigo()).collect(Collectors.toSet()));
            if (!vigente) {
                var session = request.getSession(false);
                if (session != null) session.invalidate();
                SecurityContextHolder.clearContext();
                response.sendRedirect(request.getContextPath() + "/login?sesion");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
