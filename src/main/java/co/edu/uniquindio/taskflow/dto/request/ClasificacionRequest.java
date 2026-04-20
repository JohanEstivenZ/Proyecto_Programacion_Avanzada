package co.edu.uniquindio.taskflow.dto.request;

import co.edu.uniquindio.taskflow.domain.enums.TipoSolicitud;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClasificacionRequest {

    @NotNull
    private TipoSolicitud tipo;

    @NotNull
    private Integer version;
}