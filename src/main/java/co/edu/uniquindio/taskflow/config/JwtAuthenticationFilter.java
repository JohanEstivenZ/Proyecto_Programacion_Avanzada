package co.edu.uniquindio.taskflow.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Filtro que intercepta cada peticion HTTP para extraer y validar el token JWT.
 *
 * <p>Extiende {@link OncePerRequestFilter} que garantiza ejecutarse exactamente
 * una vez por request (importante porque Spring puede invocar filtros multiples
 * veces en ciertos escenarios con forwards/redirects).</p>
 *
 * <p>Flujo:</p>
 * <ol>
 *   <li>Extrae el token del header {@code Authorization: Bearer <token>}.</li>
 *   <li>Valida la firma y la expiracion del token.</li>
 *   <li>Extrae userId, email y rol del payload del JWT.</li>
 *   <li>Crea un {@link UsernamePasswordAuthenticationToken} y lo coloca en el
 *       {@link SecurityContextHolder}. Spring Security usa este objeto para
 *       las decisiones de autorizacion ({@code hasRole}, etc.).</li>
 *   <li>Si no hay token o es invalido, continua sin autenticar.
 *       Spring Security denegara el acceso si el endpoint lo requiere.</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        String token = extraerToken(request);

        if (StringUtils.hasText(token) && jwtUtils.validarToken(token)) {
            UUID userId = jwtUtils.obtenerUsuarioId(token);
            String rol = jwtUtils.obtenerRol(token);

            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + rol)
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
