package co.edu.uniquindio.taskflow.dto.request;

import co.edu.uniquindio.taskflow.domain.enums.Categoria;
import co.edu.uniquindio.taskflow.domain.enums.Prioridad;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO para clasificar una tarea asignandole categoria y prioridad.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasificarTareaRequest {

    @NotNull(message = "La version es obligatoria")
    private Integer version;

    @NotNull(message = "La categoria es obligatoria")
    private Categoria categoria;

    @NotNull(message = "La prioridad es obligatoria")
    private Prioridad prioridad;
}
