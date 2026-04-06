package co.edu.uniquindio.taskflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO para la autenticacion de usuarios.
 *
 * <p>{@code @Email} valida el formato del correo electronico usando
 * una expresion regular interna de Hibernate Validator. No verifica
 * que el dominio exista, solo que la estructura sea valida.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es valido")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    private String password;
}
