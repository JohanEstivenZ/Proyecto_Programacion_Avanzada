package co.edu.uniquindio.taskflow.dto.request;

import co.edu.uniquindio.taskflow.domain.enums.EstadoSolicitud;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambioEstadoRequest {

    @NotNull
    private EstadoSolicitud nuevoEstado;

    @NotNull
    private Integer version;
}