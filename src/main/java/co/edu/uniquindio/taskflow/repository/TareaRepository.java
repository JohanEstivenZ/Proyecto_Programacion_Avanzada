package co.edu.uniquindio.taskflow.repository;

import co.edu.uniquindio.taskflow.domain.enums.*;
import co.edu.uniquindio.taskflow.domain.model.Tarea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad {@link Tarea}.
 *
 * <h3>JPQL (Java Persistence Query Language)</h3>
 * <p>Las queries marcadas con {@code @Query} usan JPQL, que es similar a SQL
 * pero opera sobre entidades Java, no sobre tablas directamente.</p>
 *
 * <h3>JOIN FETCH — Evitar el problema N+1</h3>
 * <p>Sin {@code JOIN FETCH}, si cargas una tarea y luego accedes a {@code tarea.getCreador()},
 * Hibernate ejecuta una query adicional (LAZY loading). Si cargas 20 tareas,
 * se ejecutan 20 queries extra para los creadores. Con {@code JOIN FETCH},
 * Hibernate trae todo en una sola query con un JOIN SQL.</p>
 *
 * <h3>Filtros dinamicos</h3>
 * <p>El patron {@code :param IS NULL OR campo = :param} permite que los
 * parametros nulos se ignoren. Asi, un solo metodo sirve para multiples
 * combinaciones de filtros sin necesidad de escribir un metodo por cada una.</p>
 */
@Repository
public interface TareaRepository extends JpaRepository<Tarea, UUID> {

    @Query("SELECT t FROM Tarea t " +
            "LEFT JOIN FETCH t.creador " +
            "LEFT JOIN FETCH t.asignado " +
            "WHERE t.id = :id")
    Optional<Tarea> findByIdConRelaciones(@Param("id") UUID id);

    @Query("SELECT DISTINCT t FROM Tarea t " +
            "LEFT JOIN FETCH t.creador " +
            "LEFT JOIN FETCH t.asignado " +
            "LEFT JOIN FETCH t.historial h " +
            "LEFT JOIN FETCH h.actor " +
            "WHERE t.id = :id")
    Optional<Tarea> findByIdConHistorial(@Param("id") UUID id);

    @Query("SELECT t FROM Tarea t " +
            "LEFT JOIN FETCH t.creador " +
            "LEFT JOIN FETCH t.asignado " +
            "WHERE (:estado IS NULL OR t.estado = :estado) " +
            "AND (:categoria IS NULL OR t.categoria = :categoria) " +
            "AND (:prioridad IS NULL OR t.prioridad = :prioridad) " +
            "AND (:asignadoId IS NULL OR t.asignado.id = :asignadoId)")
    Page<Tarea> buscarConFiltros(
            @Param("estado") EstadoTarea estado,
            @Param("categoria") Categoria categoria,
            @Param("prioridad") Prioridad prioridad,
            @Param("asignadoId") UUID asignadoId,
            Pageable pageable);

    long countByEstado(EstadoTarea estado);

    Page<Tarea> findByCreadorId(UUID creadorId, Pageable pageable);
}
