package co.edu.uniquindio.taskflow.domain.model;

import co.edu.uniquindio.taskflow.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad principal del dominio: representa una tarea del equipo.
 *
 * <h3>Domain-Driven Design (DDD)</h3>
 * <p>Esta clase es el <strong>Aggregate Root</strong> del dominio. Esto significa que:</p>
 * <ul>
 *   <li>Toda la logica de negocio vive AQUI, no en el servicio.</li>
 *   <li>Las reglas de transicion de estado se validan en los metodos
 *       {@link #iniciar()}, {@link #enviarARevision()}, etc.</li>
 *   <li>El servicio solo orquesta (buscar, validar version, guardar),
 *       pero NO contiene reglas de negocio.</li>
 * </ul>
 *
 * <h3>Anotaciones JPA relevantes</h3>
 * <ul>
 *   <li>{@code @Version} — <strong>Concurrencia optimista.</strong>
 *       Hibernate incrementa este campo en cada UPDATE.
 *       Si dos usuarios leen la misma tarea (version=3) y ambos intentan
 *       modificarla, el primero que guarde la pone en version=4.
 *       El segundo recibirá un {@code OptimisticLockingFailureException}
 *       porque su version (3) ya no coincide con la de la BD (4).
 *       Esto evita condiciones de carrera sin usar locks pesimistas.</li>
 *
 *   <li>{@code @ManyToOne(fetch = FetchType.LAZY)} — Relacion N:1.
 *       {@code LAZY} = Hibernate NO carga la entidad relacionada hasta
 *       que se acceda al getter. Esto evita queries innecesarias.
 *       Ejemplo: si solo necesitas el nombre de la tarea, no carga
 *       toda la entidad Usuario del creador.</li>
 *
 *   <li>{@code @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)} —
 *       Relacion 1:N con el historial.
 *       {@code CASCADE ALL}: al hacer {@code save(tarea)}, tambien se guardan
 *       automaticamente los HistorialTarea nuevos que se hayan agregado.
 *       {@code orphanRemoval}: si se elimina un historial de la lista,
 *       Hibernate lo borra de la BD.</li>
 *
 *   <li>{@code @OrderBy("fechaEvento ASC")} — Cuando Hibernate carga la
 *       lista de historial, la ordena cronologicamente.</li>
 *
 *   <li>{@code @Index} — Crea un indice en la BD para acelerar las consultas
 *       que filtran por ese campo. Sin indice, la BD hace un full table scan
 *       (recorre TODA la tabla). Con indice, usa una estructura de arbol
 *       (B-Tree) que encuentra los registros en O(log n).</li>
 * </ul>
 *
 * <h3>Builder.Default</h3>
 * <p>{@code @Builder.Default} le dice a Lombok que use el valor por defecto
 * (en este caso {@code new ArrayList<>()}) cuando se usa el Builder.
 * Sin esta anotacion, el Builder dejaria la lista en null.</p>
 */
@Entity
@Table(name = "tareas", indexes = {
        @Index(name = "idx_tarea_estado", columnList = "estado"),
        @Index(name = "idx_tarea_categoria", columnList = "categoria"),
        @Index(name = "idx_tarea_prioridad", columnList = "prioridad"),
        @Index(name = "idx_tarea_asignado", columnList = "asignado_id"),
        @Index(name = "idx_tarea_creador", columnList = "creador_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarea {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", nullable = false, length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoTarea estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 20)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", length = 10)
    private Prioridad prioridad;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @Column(name = "observacion_cierre", length = 1000)
    private String observacionCierre;

    // --- Relaciones ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignado_id")
    private Usuario asignado;

    @OneToMany(mappedBy = "tarea", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("fechaEvento ASC")
    @Builder.Default
    private List<HistorialTarea> historial = new ArrayList<>();

    // --- Concurrencia optimista ---

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    // --- Callback JPA ---

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoTarea.PENDIENTE;
        }
    }

    // =====================================================================
    // METODOS DE DOMINIO — Encapsulan las reglas de negocio (DDD)
    // =====================================================================

    /**
     * Clasifica la tarea asignandole categoria y prioridad.
     * Solo se permite en estado PENDIENTE.
     *
     * @param categoria tipo de trabajo
     * @param prioridad nivel de urgencia
     * @throws IllegalStateException si el estado no es PENDIENTE
     */
    public void clasificar(Categoria categoria, Prioridad prioridad) {
        if (this.estado != EstadoTarea.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se puede clasificar una tarea en estado PENDIENTE, estado actual: " + this.estado);
        }
        this.categoria = categoria;
        this.prioridad = prioridad;
    }

    /**
     * Asigna un responsable a la tarea.
     * El usuario debe estar activo en el sistema.
     *
     * @param usuario responsable a asignar
     * @throws IllegalStateException si la tarea esta ARCHIVADA
     * @throws IllegalArgumentException si el usuario no esta activo
     */
    public void asignar(Usuario usuario) {
        if (this.estado == EstadoTarea.ARCHIVADA) {
            throw new IllegalStateException("No se puede asignar una tarea archivada");
        }
        if (!usuario.getActivo()) {
            throw new IllegalArgumentException("El usuario " + usuario.getNombre() + " no esta activo");
        }
        this.asignado = usuario;
    }

    /** Transiciona de PENDIENTE a EN_PROGRESO. */
    public void iniciar() {
        validarTransicion(EstadoTarea.EN_PROGRESO);
        this.estado = EstadoTarea.EN_PROGRESO;
    }

    /** Transiciona de EN_PROGRESO a EN_REVISION. */
    public void enviarARevision() {
        validarTransicion(EstadoTarea.EN_REVISION);
        this.estado = EstadoTarea.EN_REVISION;
    }

    /** Transiciona de EN_REVISION a COMPLETADA. */
    public void completar() {
        validarTransicion(EstadoTarea.COMPLETADA);
        this.estado = EstadoTarea.COMPLETADA;
    }

    /**
     * Archiva la tarea con una observacion obligatoria de al menos 10 caracteres.
     *
     * @param observacion justificacion del archivo
     * @throws IllegalStateException si el estado no es COMPLETADA
     * @throws IllegalArgumentException si la observacion es corta o vacia
     */
    public void archivar(String observacion) {
        validarTransicion(EstadoTarea.ARCHIVADA);
        if (observacion == null || observacion.isBlank() || observacion.length() < 10) {
            throw new IllegalArgumentException(
                    "La observacion de archivo es obligatoria (minimo 10 caracteres)");
        }
        this.observacionCierre = observacion;
        this.estado = EstadoTarea.ARCHIVADA;
    }

    /**
     * Agrega una entrada al historial auditable de la tarea.
     * Establece la relacion bidireccional automaticamente.
     *
     * @param entrada registro de historial a agregar
     */
    public void agregarHistorial(HistorialTarea entrada) {
        entrada.setTarea(this);
        this.historial.add(entrada);
    }

    // --- Maquina de estados ---

    /**
     * Valida que la transicion de estado sea permitida.
     * Implementa la maquina de estados del ciclo de vida.
     *
     * <p>Se usa un switch expression (Java 14+). La flecha {@code ->}
     * reemplaza el {@code case: ... break;} y retorna directamente un valor.</p>
     *
     * @param nuevoEstado estado destino
     * @throws IllegalStateException si la transicion no es valida
     */
    private void validarTransicion(EstadoTarea nuevoEstado) {
        boolean valida = switch (nuevoEstado) {
            case EN_PROGRESO -> this.estado == EstadoTarea.PENDIENTE;
            case EN_REVISION -> this.estado == EstadoTarea.EN_PROGRESO;
            case COMPLETADA -> this.estado == EstadoTarea.EN_REVISION;
            case ARCHIVADA -> this.estado == EstadoTarea.COMPLETADA;
            default -> false;
        };

        if (!valida) {
            throw new IllegalStateException(
                    String.format("Transicion invalida: %s → %s", this.estado, nuevoEstado));
        }
    }
}
