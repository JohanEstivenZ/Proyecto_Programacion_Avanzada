package co.edu.uniquindio.taskflow.exception;

import java.util.UUID;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, UUID id) {
        super(recurso + " no encontrado con id: " + id);
    }

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}