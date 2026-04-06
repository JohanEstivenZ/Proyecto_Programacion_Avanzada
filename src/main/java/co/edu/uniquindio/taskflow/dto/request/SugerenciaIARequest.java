package co.edu.uniquindio.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO para solicitar una sugerencia de clasificacion por IA.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SugerenciaIARequest {

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(min = 10)
    private String descripcion;
}
