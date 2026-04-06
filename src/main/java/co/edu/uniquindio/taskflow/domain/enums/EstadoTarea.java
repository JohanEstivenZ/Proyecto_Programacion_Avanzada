package co.edu.uniquindio.taskflow.domain.enums;

/**
 * Estados del ciclo de vida de una tarea.
 *
 * <p>Las transiciones validas son lineales:</p>
 * <pre>
 *   PENDIENTE → EN_PROGRESO → EN_REVISION → COMPLETADA → ARCHIVADA
 * </pre>
 *
 * <p>Cada enum en Java es internamente una clase que extiende {@link Enum}.
 * Los valores (PENDIENTE, EN_PROGRESO, etc.) son instancias singleton
 * de esa clase, creadas en tiempo de carga de la JVM.</p>
 *
 * <p>Se almacenan como texto en la BD gracias a {@code @Enumerated(EnumType.STRING)}
 * en la entidad. Si se usara {@code EnumType.ORDINAL}, se guardaria el indice
 * numerico (0, 1, 2...), lo cual es fragil: reordenar los valores corrompe
 * los datos existentes en la BD.</p>
 */
public enum EstadoTarea {
    /** Tarea creada pero aun no iniciada. */
    PENDIENTE,
    /** Un desarrollador esta trabajando en ella. */
    EN_PROGRESO,
    /** Trabajo terminado, esperando aprobacion del lider. */
    EN_REVISION,
    /** Aprobada y terminada. */
    COMPLETADA,
    /** Cerrada definitivamente, no se puede modificar. */
    ARCHIVADA
}
