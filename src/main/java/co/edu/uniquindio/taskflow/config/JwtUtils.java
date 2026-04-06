package co.edu.uniquindio.taskflow.config;

import co.edu.uniquindio.taskflow.domain.model.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Utilidad para generar y validar tokens JWT (JSON Web Token).
 *
 * <p>Un JWT tiene tres partes separadas por puntos: {@code header.payload.signature}</p>
 * <ul>
 *   <li><strong>Header:</strong> algoritmo de firma (HS256).</li>
 *   <li><strong>Payload:</strong> claims (datos) del usuario — ID, email, rol, expiracion.</li>
 *   <li><strong>Signature:</strong> firma HMAC-SHA256 con la clave secreta.
 *       Garantiza que nadie altero el payload sin conocer la clave.</li>
 * </ul>
 *
 * <p>{@code @PostConstruct} se ejecuta despues de que Spring inyecta las propiedades
 * ({@code @Value}), pero antes de que el bean se use. Aqui decodificamos la clave
 * Base64 y construimos la {@link SecretKey} para firmar/verificar tokens.</p>
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secretBase64;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretBase64);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + this.expiration);
        return Jwts.builder()
                .subject(usuario.getId().toString())
                .claim("email", usuario.getEmail())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(secretKey)
                .compact();
    }

    public UUID obtenerUsuarioId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String obtenerRol(String token) {
        return parseClaims(token).get("rol", String.class);
    }

    public String obtenerEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public boolean validarToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token expirado");
        } catch (JwtException e) {
            log.warn("Token invalido: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
    }
}
