package co.edu.uniquindio.taskflow.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class HistorialResponse {
    private UUID id;
    private LocalDateTime fechaHora;
    private String accion;
    private UUID actorId;
    private String actorNombre;
    private String observacion;
}