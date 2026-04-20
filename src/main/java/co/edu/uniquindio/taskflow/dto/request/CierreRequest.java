package co.edu.uniquindio.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CierreRequest {

    @NotBlank
    private String observacionCierre;

    @NotNull
    private Integer version;
}