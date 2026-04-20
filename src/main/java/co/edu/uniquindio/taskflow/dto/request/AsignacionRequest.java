package co.edu.uniquindio.taskflow.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AsignacionRequest {

    @NotNull
    private UUID responsableId;

    @NotNull
    private Integer version;
}