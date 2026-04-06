package co.edu.uniquindio.taskflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * DTO para solicitar un resumen de tarea generado por IA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenIARequest {

    @NotNull(message = "El ID de la tarea es obligatorio")
    private UUID tareaId;
}
