package com.mycompany.proyecto_desarrollo_app_web;

import com.mycompany.proyecto_desarrollo_app_web.service.UsuarioDetailsService;
import com.mycompany.proyecto_desarrollo_app_web.service.CuentaVigenteFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SeguridadConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, UsuarioDetailsService usuarios,
            CuentaVigenteFilter cuentas) throws Exception {
        return http.userDetailsService(usuarios)
                .authorizeHttpRequests(rutas -> rutas
                    .requestMatchers("/login", "/error", "/css/**", "/js/**", "/webjars/**").permitAll()
                    .requestMatchers("/usuario/**", "/fuente-pago/**").hasRole("ADMIN")
                    .requestMatchers("/importacion/**").hasAnyRole("ADMIN", "ANALISTA")
                    .requestMatchers("/liquidacion/**", "/conciliacion/**", "/diferencia/**", "/reporte/**")
                        .hasAnyRole("ANALISTA", "SUPERVISOR")
                    .anyRequest().authenticated())
                .formLogin(form -> form.loginPage("/login").usernameParameter("correo")
                    .defaultSuccessUrl("/", true).failureUrl("/login?error").permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
                .addFilterAfter(cuentas, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
