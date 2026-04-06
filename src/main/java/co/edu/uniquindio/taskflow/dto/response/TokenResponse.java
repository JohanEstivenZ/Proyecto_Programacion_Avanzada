package co.edu.uniquindio.taskflow.dto.response;

import lombok.*;

/**
 * DTO de salida con el token JWT y datos basicos del usuario autenticado.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TokenResponse {
    private String token;
    private String tipo;
    private String email;
    private String nombre;
    private String rol;
}
