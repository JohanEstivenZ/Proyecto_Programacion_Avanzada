package co.edu.uniquindio.taskflow.dto.response;

import co.edu.uniquindio.taskflow.domain.enums.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de salida que representa una tarea completa.
 * Es lo que el cliente recibe como respuesta JSON.
 *
 * <p>Notese que NO exponemos el password del creador ni del asignado,
 * solo sus IDs y nombres. Este es el beneficio del patron DTO:
 * controlar exactamente que datos salen hacia el cliente.</p>
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TareaResponse {
    private UUID id;
    private String titulo;
    private String descripcion;
    private EstadoTarea estado;
    private Categoria categoria;
    private Prioridad prioridad;
    private String observacionCierre;
    private UUID creadorId;
    private String creadorNombre;
    private UUID asignadoId;
    private String asignadoNombre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaLimite;
    private Integer version;
    private List<HistorialResponse> historial;
}
