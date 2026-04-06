package co.edu.uniquindio.taskflow.exception;

/**
 * Lanzada cuando se viola una regla de negocio (email duplicado, conflicto de version, etc.).
 * El {@code GlobalExceptionHandler} la intercepta y retorna HTTP 422.
 */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String message) {
        super(message);
    }
}
