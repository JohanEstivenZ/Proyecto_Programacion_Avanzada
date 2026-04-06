package co.edu.uniquindio.taskflow.dto.response;

import lombok.*;

/**
 * DTO de salida con el resumen de tarea generado por IA.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResumenIAResponse {
    private String resumen;
    private String generadoPor;
}
