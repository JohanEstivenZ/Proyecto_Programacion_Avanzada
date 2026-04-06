package co.edu.uniquindio.taskflow.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de salida para cada entrada del historial auditable.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistorialResponse {
    private UUID id;
    private LocalDateTime fechaEvento;
    private String accion;
    private UUID actorId;
    private String actorNombre;
    private String observacion;
}
