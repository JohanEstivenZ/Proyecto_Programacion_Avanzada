package co.edu.uniquindio.taskflow.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuracion central de Spring Security.
 *
 * <p>{@code @EnableWebSecurity} activa la seguridad web de Spring.</p>
 * <p>{@code @EnableMethodSecurity} permite usar {@code @PreAuthorize} en metodos.</p>
 *
 * <h3>Decisiones de diseno</h3>
 * <ul>
 *   <li><strong>CSRF deshabilitado:</strong> nuestra API es stateless (JWT, no cookies).
 *       CSRF solo aplica cuando el navegador envia cookies automaticamente.</li>
 *   <li><strong>SessionCreationPolicy.STATELESS:</strong> Spring no crea HttpSession.
 *       Cada request se autentica independientemente mediante el JWT.</li>
 *   <li><strong>BCrypt:</strong> algoritmo de hash adaptativo. Cada hash incluye
 *       un salt aleatorio (no se repiten). El factor de costo (10) controla
 *       cuantas iteraciones se hacen — mas lento = mas seguro contra fuerza bruta.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/tareas").authenticated()
                        .requestMatchers(HttpMethod.GET, "/tareas/**").authenticated()

                        .requestMatchers(HttpMethod.PATCH, "/tareas/*/clasificar").hasAnyRole("TEAM_LEAD", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/tareas/*/asignar").hasAnyRole("TEAM_LEAD", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/tareas/*/iniciar").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/tareas/*/revision").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/tareas/*/completar").hasAnyRole("TEAM_LEAD", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/tareas/*/archivar").hasAnyRole("TEAM_LEAD", "ADMIN")

                        .requestMatchers("/ia/**").authenticated()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
