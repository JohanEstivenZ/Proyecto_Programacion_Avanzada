package co.edu.uniquindio.taskflow.domain.model;

import co.edu.uniquindio.taskflow.domain.enums.Rol;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Entidad JPA que representa un usuario del sistema TaskFlow.
 *
 * <h3>Anotaciones JPA</h3>
 * <ul>
 *   <li>{@code @Entity} — Marca esta clase como una entidad gestionada por JPA.
 *       Hibernate la mapeara a una tabla en la base de datos. Cada instancia
 *       de esta clase corresponde a una fila en la tabla.</li>
 *   <li>{@code @Table(name = "usuarios")} — Especifica el nombre exacto de la tabla.
 *       Sin esta anotacion, Hibernate usaria el nombre de la clase ("Usuario").</li>
 *   <li>{@code @Id} — Indica cual campo es la clave primaria (PK) de la tabla.</li>
 *   <li>{@code @UuidGenerator} — Genera un UUID (Universally Unique Identifier)
 *       automaticamente al hacer {@code save()}. UUID v4 usa 128 bits aleatorios,
 *       lo que hace practicamente imposible la colision incluso en sistemas
 *       distribuidos.</li>
 *   <li>{@code @Column} — Configura la columna: nombre, si acepta nulls,
 *       longitud maxima, si es unica, si se puede actualizar.</li>
 *   <li>{@code @Enumerated(EnumType.STRING)} — Persiste el enum como texto
 *       ("ADMIN") en vez de ordinal (2). Siempre usar STRING.</li>
 *   <li>{@code @PrePersist} — Metodo callback que se ejecuta automaticamente
 *       ANTES de que Hibernate haga el INSERT en la BD. Ideal para asignar
 *       valores por defecto.</li>
 * </ul>
 *
 * <h3>Anotaciones Lombok</h3>
 * <ul>
 *   <li>{@code @Getter} / {@code @Setter} — Genera automaticamente todos los
 *       getters y setters en tiempo de compilacion. El .class resultante los
 *       tiene, aunque no los veas en el codigo fuente.</li>
 *   <li>{@code @Builder} — Genera el patron Builder, que permite construir
 *       objetos de forma fluida:
 *       {@code Usuario.builder().nombre("Ana").email("ana@test.com").build()}</li>
 *   <li>{@code @NoArgsConstructor} — Constructor sin argumentos.
 *       <strong>Requerido por JPA</strong>: Hibernate necesita crear instancias
 *       vacias para luego llenarlas con los datos de la BD.</li>
 *   <li>{@code @AllArgsConstructor} — Constructor con todos los campos.
 *       <strong>Requerido por @Builder</strong>: el Builder lo necesita
 *       internamente para construir el objeto.</li>
 * </ul>
 *
 * <h3>Por que UUID y no Long auto-increment?</h3>
 * <p>UUID no depende de la base de datos para generarse. Se puede crear en
 * la aplicacion antes del INSERT. Esto es util en microservicios, replicacion,
 * y evita exponer IDs secuenciales (seguridad por oscuridad).</p>
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "identificacion", unique = true, nullable = false, length = 20)
    private String identificacion;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "email", unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private Rol rol;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    /**
     * Callback JPA que se ejecuta antes del primer INSERT.
     * Si no se asigno un valor a {@code activo}, lo pone en {@code true}.
     */
    @PrePersist
    public void prePersist() {
        if (activo == null) {
            activo = true;
        }
    }
}
