package co.edu.uniquindio.taskflow.service.impl;

import co.edu.uniquindio.taskflow.domain.enums.*;
import co.edu.uniquindio.taskflow.domain.model.*;
import co.edu.uniquindio.taskflow.dto.request.*;
import co.edu.uniquindio.taskflow.dto.response.*;
import co.edu.uniquindio.taskflow.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.taskflow.exception.ReglaNegocioException;
import co.edu.uniquindio.taskflow.repository.*;
import co.edu.uniquindio.taskflow.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SolicitudServiceImpl implements SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public SolicitudResponse crear(SolicitudCreacionRequest request, String emailSolicitante) {
        log.info("Creando solicitud para usuario: {}", emailSolicitante);

        Usuario solicitante = usuarioRepository.findByEmail(emailSolicitante)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        Solicitud solicitud = Solicitud.builder()
                .tipo(request.getTipo())
                .descripcion(request.getDescripcion())
                .canalOrigen(request.getCanalOrigen())
                .estado(EstadoSolicitud.REGISTRADA)
                .solicitante(solicitante)
                .build();

        HistorialSolicitud historial = HistorialSolicitud.builder()
                .accion("SOLICITUD_REGISTRADA")
                .actor(solicitante)
                .observacion("Solicitud registrada: " + request.getTipo())
                .fechaHora(LocalDateTime.now())
                .build();
        solicitud.agregarHistorial(historial);

        solicitud = solicitudRepository.save(solicitud);
        log.info("Solicitud creada con ID: {}", solicitud.getId());
        return toResponse(solicitud);
    }

    @Override
    public SolicitudResponse clasificar(UUID solicitudId, ClasificacionRequest request, UUID actorId) {
        log.info("Clasificando solicitud: {}", solicitudId);

        Solicitud solicitud = obtenerSolicitudConVersion(solicitudId, request.getVersion());
        Usuario actor = obtenerUsuario(actorId);

        solicitud.clasificar(request.getTipo());

        HistorialSolicitud historial = HistorialSolicitud.builder()
                .accion("SOLICITUD_CLASIFICADA")
                .actor(actor)
                .observacion("Tipo asignado: " + request.getTipo())
                .fechaHora(LocalDateTime.now())
                .build();
        solicitud.agregarHistorial(historial);

        solicitud = solicitudRepository.save(solicitud);
        return toResponseConHistorial(solicitud);
    }

    @Override
    public SolicitudResponse priorizar(UUID solicitudId, PrioridadRequest request, UUID actorId) {
        log.info("Priorizando solicitud: {}", solicitudId);

        Solicitud solicitud = obtenerSolicitudConVersion(solicitudId, request.getVersion());
        Usuario actor = obtenerUsuario(actorId);

        solicitud.priorizar(request.getPrioridad(), request.getJustificacion());

        HistorialSolicitud historial = HistorialSolicitud.builder()
                .accion("SOLICITUD_PRIORIZADA")
                .actor(actor)
                .observacion("Prioridad: " + request.getPrioridad() + " — " + request.getJustificacion())
                .fechaHora(LocalDateTime.now())
                .build();
        solicitud.agregarHistorial(historial);

        solicitud = solicitudRepository.save(solicitud);
        return toResponseConHistorial(solicitud);
    }

    @Override
    public SolicitudResponse asignarResponsable(UUID solicitudId, AsignacionRequest request, UUID actorId) {
        log.info("Asignando responsable a solicitud: {}", solicitudId);

        Solicitud solicitud = obtenerSolicitudConVersion(solicitudId, request.getVersion());
        Usuario actor = obtenerUsuario(actorId);
        Usuario responsable = usuarioRepository.findById(request.getResponsableId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Responsable", request.getResponsableId()));

        solicitud.asignarResponsable(responsable);

        HistorialSolicitud historial = HistorialSolicitud.builder()
                .accion("RESPONSABLE_ASIGNADO")
                .actor(actor)
                .observacion("Asignada a: " + responsable.getNombre())
                .fechaHora(LocalDateTime.now())
                .build();
        solicitud.agregarHistorial(historial);

        solicitud = solicitudRepository.save(solicitud);
        return toResponseConHistorial(solicitud);
    }

    @Override
    public SolicitudResponse cambiarEstado(UUID solicitudId, CambioEstadoRequest request, UUID actorId) {
        return transicionar(solicitudId, request.getVersion(), actorId,
                "ESTADO_CAMBIADO", "Estado cambiado a: " + request.getNuevoEstado(),
                s -> s.cambiarEstado(request.getNuevoEstado()));
    }

    @Override
    public SolicitudResponse cerrar(UUID solicitudId, CierreRequest request, UUID actorId) {
        log.info("Cerrando solicitud: {}", solicitudId);

        Solicitud solicitud = obtenerSolicitudConVersion(solicitudId, request.getVersion());
        Usuario actor = obtenerUsuario(actorId);

        solicitud.cerrar(request.getObservacionCierre());

        HistorialSolicitud historial = HistorialSolicitud.builder()
                .accion("SOLICITUD_CERRADA")
                .actor(actor)
                .observacion(request.getObservacionCierre())
                .fechaHora(LocalDateTime.now())
                .build();
        solicitud.agregarHistorial(historial);

        solicitud = solicitudRepository.save(solicitud);
        return toResponseConHistorial(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudResponse obtenerPorId(UUID solicitudId) {
        Solicitud solicitud = solicitudRepository.findByIdConHistorial(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud", solicitudId));
        return toResponseConHistorial(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SolicitudResponse> listar(EstadoSolicitud estado, TipoSolicitud tipo,
                                          Prioridad prioridad, UUID responsableId, Pageable pageable) {
        return solicitudRepository
                .buscarConFiltros(estado, tipo, prioridad, responsableId, pageable)
                .map(this::toResponse);
    }

    //  Métodos privados

    private SolicitudResponse transicionar(UUID solicitudId, Integer version, UUID actorId,
                                           String accion, String observacion,
                                           java.util.function.Consumer<Solicitud> transicion) {
        Solicitud solicitud = obtenerSolicitudConVersion(solicitudId, version);
        Usuario actor = obtenerUsuario(actorId);

        transicion.accept(solicitud);

        HistorialSolicitud historial = HistorialSolicitud.builder()
                .accion(accion)
                .actor(actor)
                .observacion(observacion)
                .fechaHora(LocalDateTime.now())
                .build();
        solicitud.agregarHistorial(historial);

        solicitud = solicitudRepository.save(solicitud);
        return toResponseConHistorial(solicitud);
    }

    private Solicitud obtenerSolicitudConVersion(UUID solicitudId, Integer versionCliente) {
        Solicitud solicitud = solicitudRepository.findByIdConRelaciones(solicitudId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud", solicitudId));

        if (!solicitud.getVersion().equals(versionCliente)) {
            throw new ReglaNegocioException(
                    "Conflicto de concurrencia: versión esperada " + versionCliente
                            + ", versión actual " + solicitud.getVersion());
        }
        return solicitud;
    }

    private Usuario obtenerUsuario(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    private SolicitudResponse toResponse(Solicitud s) {
        return SolicitudResponse.builder()
                .id(s.getId())
                .tipo(s.getTipo())
                .descripcion(s.getDescripcion())
                .canalOrigen(s.getCanalOrigen())
                .fechaRegistro(s.getFechaRegistro())
                .estado(s.getEstado())
                .prioridad(s.getPrioridad())
                .justificacionPrioridad(s.getJustificacionPrioridad())
                .observacionCierre(s.getObservacionCierre())
                .solicitanteId(s.getSolicitante().getId())
                .solicitanteNombre(s.getSolicitante().getNombre())
                .responsableId(s.getResponsable() != null ? s.getResponsable().getId() : null)
                .responsableNombre(s.getResponsable() != null ? s.getResponsable().getNombre() : null)
                .version(s.getVersion())
                .historial(Collections.emptyList())
                .build();
    }

    private SolicitudResponse toResponseConHistorial(Solicitud s) {
        SolicitudResponse response = toResponse(s);
        List<HistorialResponse> historial = s.getHistorial().stream()
                .map(h -> HistorialResponse.builder()
                        .id(h.getId())
                        .fechaHora(h.getFechaHora())
                        .accion(h.getAccion())
                        .actorId(h.getActor().getId())
                        .actorNombre(h.getActor().getNombre())
                        .observacion(h.getObservacion())
                        .build())
                .collect(Collectors.toList());
        response.setHistorial(historial);
        return response;
    }
}