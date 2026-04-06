package co.edu.uniquindio.taskflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * DTO para asignar un responsable a una tarea.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarTareaRequest {

    @NotNull(message = "La version es obligatoria")
    private Integer version;

    @NotNull(message = "El ID del responsable es obligatorio")
    private UUID asignadoId;
}
