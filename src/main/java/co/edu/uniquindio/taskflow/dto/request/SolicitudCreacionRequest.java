package co.edu.uniquindio.taskflow.dto.request;

import co.edu.uniquindio.taskflow.domain.enums.CanalOrigen;
import co.edu.uniquindio.taskflow.domain.enums.TipoSolicitud;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SolicitudCreacionRequest {

    @NotNull
    private TipoSolicitud tipo;

    @NotBlank
    private String descripcion;

    @NotNull
    private CanalOrigen canalOrigen;

}