package resourcebooking.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Desactivamos CSRF porque es una API REST (importante para POST/DELETE)
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // 1. REGLA DE ORO: El test de Redis es público (sin token)
                        .requestMatchers("/api/email-test").permitAll()

                        // Permitir todos los GETs (para ver bookings)
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                        // 4. Cualquier otra cosa, cerrada
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}