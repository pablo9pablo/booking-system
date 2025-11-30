package resourcebooking.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Definir qué rutas son públicas y cuáles privadas
                .authorizeHttpRequests(authorize -> authorize
                        // Cualquiera puede VER las reservas (GET)
                        .requestMatchers("/api/bookings").permitAll()
                        // Solo usuarios autenticados pueden CREAR reservas (POST)
                        .requestMatchers("/api/book").authenticated()
                        // Cualquier otra cosa también requiere autenticación
                        .anyRequest().authenticated()
                )
                // 2. Configurar que usaremos tokens JWT (Auth0)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}