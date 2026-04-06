package co.edu.uniquindio.taskflow.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que registra cada accion realizada sobre una tarea.
 *
 * <p>Forma el <strong>historial auditable</strong>: cada vez que alguien
 * crea, clasifica, asigna, inicia o archiva una tarea, se agrega
 * una entrada aqui. Esto permite reconstruir la linea de tiempo
 * completa de cualquier tarea.</p>
 *
 * <p>Las entradas son <strong>inmutables</strong> una vez creadas:
 * no tienen setters para {@code fechaEvento} ni {@code accion}
 * (Lombok los genera, pero por convencion no se usan para modificar).</p>
 *
 * <h3>{@code @ManyToOne(fetch = FetchType.LAZY)}</h3>
 * <p>Tanto {@code actor} como {@code tarea} se cargan de forma perezosa.
 * Cuando haces {@code historial.getActor()}, Hibernate ejecuta un SELECT
 * en ese momento (no antes). Esto es eficiente porque no siempre
 * necesitas los datos del actor al cargar el historial.</p>
 *
 * <p>Para evitar el problema de N+1 queries (1 query por historial +
 * N queries por actor), los repositorios usan {@code JOIN FETCH}
 * en sus consultas JPQL.</p>
 */
@Entity
@Table(name = "historial_tareas", indexes = {
        @Index(name = "idx_historial_tarea", columnList = "tarea_id"),
        @Index(name = "idx_historial_fecha", columnList = "fecha_evento")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialTarea {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "fecha_evento", nullable = false, updatable = false)
    private LocalDateTime fechaEvento;

    @Column(name = "accion", nullable = false, length = 100)
    private String accion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private Usuario actor;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarea_id", nullable = false)
    private Tarea tarea;

    @PrePersist
    public void prePersist() {
        if (fechaEvento == null) {
            fechaEvento = LocalDateTime.now();
        }
    }
}
