package co.edu.uniquindio.taskflow.repository;

import co.edu.uniquindio.taskflow.domain.enums.*;
import co.edu.uniquindio.taskflow.domain.model.Solicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, UUID> {

    @Query("SELECT s FROM Solicitud s " +
            "LEFT JOIN FETCH s.solicitante " +
            "LEFT JOIN FETCH s.responsable " +
            "WHERE s.id = :id")
    Optional<Solicitud> findByIdConRelaciones(@Param("id") UUID id);

    @Query("SELECT DISTINCT s FROM Solicitud s " +
            "LEFT JOIN FETCH s.solicitante " +
            "LEFT JOIN FETCH s.responsable " +
            "LEFT JOIN FETCH s.historial h " +
            "LEFT JOIN FETCH h.actor " +
            "WHERE s.id = :id")
    Optional<Solicitud> findByIdConHistorial(@Param("id") UUID id);

    @Query("SELECT s FROM Solicitud s " +
            "LEFT JOIN FETCH s.solicitante " +
            "LEFT JOIN FETCH s.responsable " +
            "WHERE (:estado IS NULL OR s.estado = :estado) " +
            "AND (:tipo IS NULL OR s.tipo = :tipo) " +
            "AND (:prioridad IS NULL OR s.prioridad = :prioridad) " +
            "AND (:responsableId IS NULL OR s.responsable.id = :responsableId)")
    Page<Solicitud> buscarConFiltros(
            @Param("estado") EstadoSolicitud estado,
            @Param("tipo") TipoSolicitud tipo,
            @Param("prioridad") Prioridad prioridad,
            @Param("responsableId") UUID responsableId,
            Pageable pageable);

    long countByEstado(EstadoSolicitud estado);
}