package co.edu.uniquindio.taskflow.domain.model;

import co.edu.uniquindio.taskflow.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "solicitudes", indexes = {
        @Index(name = "idx_solicitud_estado",    columnList = "estado"),
        @Index(name = "idx_solicitud_tipo",      columnList = "tipo"),
        @Index(name = "idx_solicitud_prioridad", columnList = "prioridad"),
        @Index(name = "idx_solicitud_responsable", columnList = "responsable_id"),
        @Index(name = "idx_solicitud_solicitante", columnList = "solicitante_id")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Solicitud {

    @Id
    @UuidGenerator
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSolicitud tipo;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalOrigen canalOrigen;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    @Enumerated(EnumType.STRING)
    private Prioridad prioridad;

    private String justificacionPrioridad;

    private String observacionCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("fechaHora ASC")
    @Builder.Default
    private List<HistorialSolicitud> historial = new ArrayList<>();

    @Version
    @Column(nullable = false)
    private Integer version;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (estado == null) estado = EstadoSolicitud.REGISTRADA;
    }

    // Métodos de negocio

    public void clasificar(TipoSolicitud tipo) {
        if (this.estado != EstadoSolicitud.REGISTRADA) {
            throw new IllegalStateException(
                    "Solo se puede clasificar una solicitud REGISTRADA, estado actual: " + this.estado);
        }
        this.tipo = tipo;
        this.estado = EstadoSolicitud.CLASIFICADA;
    }

    public void priorizar(Prioridad prioridad, String justificacion) {
        this.prioridad = prioridad;
        this.justificacionPrioridad = justificacion;
    }

    public void asignarResponsable(Usuario responsable) {
        if (!responsable.isActivo()) {
            throw new IllegalArgumentException(
                    "El responsable " + responsable.getNombre() + " no está activo");
        }
        this.responsable = responsable;
        this.estado = EstadoSolicitud.EN_ATENCION;
    }

    public void cambiarEstado(EstadoSolicitud nuevoEstado) {
        if (!puedeTransicionarA(nuevoEstado)) {
            throw new IllegalStateException(
                    String.format("Transición inválida: %s → %s", this.estado, nuevoEstado));
        }
        this.estado = nuevoEstado;
    }

    public void cerrar(String observacion) {
        if (this.estado != EstadoSolicitud.ATENDIDA) {
            throw new IllegalStateException("Solo se pueden cerrar solicitudes ATENDIDAS");
        }
        if (observacion == null || observacion.isBlank() || observacion.length() < 10) {
            throw new IllegalArgumentException(
                    "La observación de cierre es obligatoria (mínimo 10 caracteres)");
        }
        this.observacionCierre = observacion;
        this.estado = EstadoSolicitud.CERRADA;
    }

    public boolean puedeTransicionarA(EstadoSolicitud nuevoEstado) {
        return switch (this.estado) {
            case REGISTRADA  -> nuevoEstado == EstadoSolicitud.CLASIFICADA;
            case CLASIFICADA -> nuevoEstado == EstadoSolicitud.EN_ATENCION;
            case EN_ATENCION -> nuevoEstado == EstadoSolicitud.ATENDIDA;
            case ATENDIDA    -> nuevoEstado == EstadoSolicitud.CERRADA;
            case CERRADA     -> false;
        };
    }

    public void agregarHistorial(HistorialSolicitud entrada) {
        entrada.setSolicitud(this);
        this.historial.add(entrada);
    }
}