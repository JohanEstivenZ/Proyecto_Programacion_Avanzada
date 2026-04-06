package co.edu.uniquindio.taskflow.domain.enums;

/**
 * Categorias para clasificar las tareas del equipo.
 * Permiten filtrar el backlog y generar metricas por tipo de trabajo.
 */
public enum Categoria {
    /** Correccion de un defecto existente. */
    BUG,
    /** Funcionalidad nueva solicitada por el cliente. */
    FEATURE,
    /** Refactorizacion o mejora de rendimiento. */
    MEJORA,
    /** Redaccion o actualizacion de documentacion tecnica. */
    DOCUMENTACION,
    /** Investigacion tecnica (spike) para evaluar viabilidad. */
    INVESTIGACION
}
