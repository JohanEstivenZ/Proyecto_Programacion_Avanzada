package co.edu.uniquindio.taskflow.service;

import co.edu.uniquindio.taskflow.domain.enums.*;
import co.edu.uniquindio.taskflow.dto.request.*;
import co.edu.uniquindio.taskflow.dto.response.SolicitudResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface SolicitudService {

    SolicitudResponse crear(SolicitudCreacionRequest request, String emailSolicitante);

    SolicitudResponse clasificar(UUID solicitudId, ClasificacionRequest request, UUID actorId);

    SolicitudResponse priorizar(UUID solicitudId, PrioridadRequest request, UUID actorId);

    SolicitudResponse asignarResponsable(UUID solicitudId, AsignacionRequest request, UUID actorId);

    SolicitudResponse cambiarEstado(UUID solicitudId, CambioEstadoRequest request, UUID actorId);

    SolicitudResponse cerrar(UUID solicitudId, CierreRequest request, UUID actorId);

    SolicitudResponse obtenerPorId(UUID solicitudId);

    Page<SolicitudResponse> listar(EstadoSolicitud estado, TipoSolicitud tipo,
                                   Prioridad prioridad, UUID responsableId, Pageable pageable);
}