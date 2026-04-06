package co.edu.uniquindio.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para crear una tarea nueva.
 *
 * <p>Los DTOs separan lo que el cliente envia de lo que la entidad almacena.
 * Ventajas de este patron:</p>
 * <ul>
 *   <li>El cliente no necesita conocer la estructura interna de la entidad.</li>
 *   <li>Se pueden validar los datos de entrada sin ensuciar la entidad.</li>
 *   <li>Se evita exponer campos sensibles (password, version, IDs internos).</li>
 * </ul>
 *
 * <h3>Anotaciones de Bean Validation (Jakarta Validation)</h3>
 * <ul>
 *   <li>{@code @NotBlank} — El campo no puede ser null, vacio (""), ni
 *       solo espacios ("   "). Mas estricto que {@code @NotNull}.</li>
 *   <li>{@code @Size(min, max)} — Restringe la longitud del String.
 *       Se valida automaticamente cuando el controller recibe el body
 *       gracias a la anotacion {@code @Valid} en el parametro.</li>
 * </ul>
 *
 * <p>Si la validacion falla, Spring lanza {@code MethodArgumentNotValidException},
 * que el {@code GlobalExceptionHandler} intercepta y transforma en un
 * response 400 con los detalles del error.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TareaCrearRequest {

    @NotBlank(message = "El titulo es obligatorio")
    @Size(min = 5, max = 200, message = "El titulo debe tener entre 5 y 200 caracteres")
    private String titulo;

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripcion debe tener entre 10 y 2000 caracteres")
    private String descripcion;

    private LocalDateTime fechaLimite;
}
