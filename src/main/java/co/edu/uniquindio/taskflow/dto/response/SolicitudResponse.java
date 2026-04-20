package co.edu.uniquindio.taskflow.dto.response;

import co.edu.uniquindio.taskflow.domain.enums.*;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class SolicitudResponse {
    private UUID id;
    private TipoSolicitud tipo;
    private String descripcion;
    private CanalOrigen canalOrigen;
    private LocalDateTime fechaRegistro;
    private EstadoSolicitud estado;
    private Prioridad prioridad;
    private String justificacionPrioridad;
    private String observacionCierre;
    private UUID solicitanteId;
    private String solicitanteNombre;
    private UUID responsableId;
    private String responsableNombre;
    private Integer version;
    private List<HistorialResponse> historial;
}