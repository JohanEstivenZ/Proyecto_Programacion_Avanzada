package co.edu.uniquindio.taskflow.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO estandarizado para respuestas de error.
 *
 * <p>{@code @JsonInclude(NON_NULL)} hace que Jackson omita los campos
 * con valor null del JSON de respuesta. Asi, si no hay errores de
 * validacion, el campo {@code validationErrors} simplemente no aparece
 * en el JSON, en vez de aparecer como {@code "validationErrors": null}.</p>
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;
}
