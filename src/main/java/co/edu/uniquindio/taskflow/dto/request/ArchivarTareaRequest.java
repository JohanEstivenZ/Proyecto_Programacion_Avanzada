package co.edu.uniquindio.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para archivar una tarea con observacion obligatoria.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchivarTareaRequest {

    @NotNull(message = "La version es obligatoria")
    private Integer version;

    @NotBlank(message = "La observacion de archivo es obligatoria")
    @Size(min = 10, message = "La observacion debe tener al menos 10 caracteres")
    private String observacion;
}
