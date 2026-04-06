package co.edu.uniquindio.taskflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO base que transporta la version actual conocida por el cliente.
 * Se usa en todas las operaciones de escritura para control de concurrencia optimista.
 *
 * <p>Flujo: el cliente lee una tarea con version=3. Cuando quiere modificarla,
 * envia {@code "version": 3} en el body. El servidor verifica que la version
 * en la BD siga siendo 3. Si otro usuario la modifico (ahora es 4),
 * se rechaza con 409 Conflict.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersionRequest {

    @NotNull(message = "La version es obligatoria para control de concurrencia")
    private Integer version;
}
