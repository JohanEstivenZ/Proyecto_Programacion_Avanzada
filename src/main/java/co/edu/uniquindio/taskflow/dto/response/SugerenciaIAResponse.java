package co.edu.uniquindio.taskflow.dto.response;

import co.edu.uniquindio.taskflow.domain.enums.Categoria;
import co.edu.uniquindio.taskflow.domain.enums.Prioridad;
import lombok.*;

/**
 * DTO de salida con la sugerencia de clasificacion generada por IA.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SugerenciaIAResponse {
    private Categoria categoriaSugerida;
    private Prioridad prioridadSugerida;
    private Float confianza;
    private String explicacion;
}
