package co.edu.uniquindio.taskflow.dto.request;

import co.edu.uniquindio.taskflow.domain.enums.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrioridadRequest {

    @NotNull
    private Prioridad prioridad;

    @NotBlank
    private String justificacion;

    @NotNull
    private Integer version;
}