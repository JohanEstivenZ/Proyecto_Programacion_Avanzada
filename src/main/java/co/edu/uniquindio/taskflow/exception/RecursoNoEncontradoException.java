package co.edu.uniquindio.taskflow.exception;

import java.util.UUID;

/**
 * Lanzada cuando se busca un recurso por ID y no existe en la BD.
 * El {@code GlobalExceptionHandler} la intercepta y retorna HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String recurso, UUID id) {
        super(String.format("%s con ID %s no fue encontrado", recurso, id));
    }
}
