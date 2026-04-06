package co.edu.uniquindio.taskflow.service.impl;

import co.edu.uniquindio.taskflow.domain.enums.*;
import co.edu.uniquindio.taskflow.domain.model.*;
import co.edu.uniquindio.taskflow.dto.request.*;
import co.edu.uniquindio.taskflow.dto.response.*;
import co.edu.uniquindio.taskflow.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.taskflow.exception.ReglaNegocioException;
import co.edu.uniquindio.taskflow.repository.TareaRepository;
import co.edu.uniquindio.taskflow.repository.UsuarioRepository;
import co.edu.uniquindio.taskflow.service.TareaService;
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

/**
 * Implementacion del servicio de tareas.
 *
 * <h3>Principios de diseno aplicados</h3>
 * <ul>
 *   <li><strong>DDD (Domain-Driven Design):</strong> la logica de negocio
 *       vive en la entidad {@link Tarea}, no aqui. Este servicio solo
 *       orquesta: buscar en BD, validar version, delegar al dominio, guardar.</li>
 *   <li><strong>Concurrencia optimista:</strong> antes de cada operacion de
 *       escritura, se verifica que la version del cliente coincida con la
 *       version en la BD. Si no coincide, otro usuario la modifico.</li>
 *   <li><strong>Historial auditable:</strong> cada transicion queda registrada
 *       en {@link HistorialTarea} con fecha, accion, actor y observacion.</li>
 * </ul>
 *
 * <h3>{@code @Transactional}</h3>
 * <p>Sin parametros: la transaccion es de lectura-escritura. Si ocurre una
 * excepcion no checked (RuntimeException o hija), se hace rollback.</p>
 *
 * <p>{@code @Transactional(readOnly = true)}: optimiza las consultas. Hibernate
 * desactiva el "dirty checking" (no compara el objeto en memoria con el de la BD
 * al final de la transaccion), lo que mejora el rendimiento en lecturas.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TareaServiceImpl implements TareaService {

    private final TareaRepository tareaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public TareaResponse crear(TareaCrearRequest request, UUID creadorId) {
        log.info("Creando tarea para usuario: {}", creadorId);

        Usuario creador = obtenerUsuario(creadorId);

        Tarea tarea = Tarea.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .creador(creador)
                .estado(EstadoTarea.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .fechaLimite(request.getFechaLimite())
                .build();

        HistorialTarea historial = HistorialTarea.builder()
                .accion("TAREA_CREADA")
                .actor(creador)
                .observacion("Tarea creada: " + request.getTitulo())
                .fechaEvento(LocalDateTime.now())
                .build();
        tarea.agregarHistorial(historial);

        tarea = tareaRepository.save(tarea);
        log.info("Tarea creada con ID: {}", tarea.getId());
        return toResponse(tarea);
    }

    @Override
    public TareaResponse clasificar(UUID tareaId, ClasificarTareaRequest request, UUID actorId) {
        log.info("Clasificando tarea: {}", tareaId);

        Tarea tarea = obtenerTareaConVersion(tareaId, request.getVersion());
        Usuario actor = obtenerUsuario(actorId);

        tarea.clasificar(request.getCategoria(), request.getPrioridad());

        HistorialTarea historial = HistorialTarea.builder()
                .accion("TAREA_CLASIFICADA")
                .actor(actor)
                .observacion(String.format("Categoria: %s, Prioridad: %s",
                        request.getCategoria(), request.getPrioridad()))
                .fechaEvento(LocalDateTime.now())
                .build();
        tarea.agregarHistorial(historial);

        tarea = tareaRepository.save(tarea);
        return toResponseConHistorial(tarea);
    }

    @Override
    public TareaResponse asignar(UUID tareaId, AsignarTareaRequest request, UUID actorId) {
        log.info("Asignando tarea {} a {}", tareaId, request.getAsignadoId());

        Tarea tarea = obtenerTareaConVersion(tareaId, request.getVersion());
        Usuario actor = obtenerUsuario(actorId);
        Usuario asignado = usuarioRepository.findById(request.getAsignadoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", request.getAsignadoId()));

        tarea.asignar(asignado);

        HistorialTarea historial = HistorialTarea.builder()
                .accion("TAREA_ASIGNADA")
                .actor(actor)
                .observacion("Asignada a: " + asignado.getNombre())
                .fechaEvento(LocalDateTime.now())
                .build();
        tarea.agregarHistorial(historial);

        tarea = tareaRepository.save(tarea);
        return toResponseConHistorial(tarea);
    }

    @Override
    public TareaResponse iniciar(UUID tareaId, VersionRequest request, UUID actorId) {
        return transicionar(tareaId, request.getVersion(), actorId, "TAREA_INICIADA",
                "Se inicio el trabajo en la tarea", Tarea::iniciar);
    }

    @Override
    public TareaResponse enviarARevision(UUID tareaId, VersionRequest request, UUID actorId) {
        return transicionar(tareaId, request.getVersion(), actorId, "TAREA_EN_REVISION",
                "Tarea enviada a revision del lider", Tarea::enviarARevision);
    }

    @Override
    public TareaResponse completar(UUID tareaId, VersionRequest request, UUID actorId) {
        return transicionar(tareaId, request.getVersion(), actorId, "TAREA_COMPLETADA",
                "Tarea aprobada y completada", Tarea::completar);
    }

    @Override
    public TareaResponse archivar(UUID tareaId, ArchivarTareaRequest request, UUID actorId) {
        log.info("Archivando tarea: {}", tareaId);

        Tarea tarea = obtenerTareaConVersion(tareaId, request.getVersion());
        Usuario actor = obtenerUsuario(actorId);

        tarea.archivar(request.getObservacion());

        HistorialTarea historial = HistorialTarea.builder()
                .accion("TAREA_ARCHIVADA")
                .actor(actor)
                .observacion(request.getObservacion())
                .fechaEvento(LocalDateTime.now())
                .build();
        tarea.agregarHistorial(historial);

        tarea = tareaRepository.save(tarea);
        return toResponseConHistorial(tarea);
    }

    @Override
    @Transactional(readOnly = true)
    public TareaResponse obtenerPorId(UUID tareaId) {
        Tarea tarea = tareaRepository.findByIdConHistorial(tareaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tarea", tareaId));
        return toResponseConHistorial(tarea);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TareaResponse> listar(EstadoTarea estado, Categoria categoria,
                                       Prioridad prioridad, UUID asignadoId, Pageable pageable) {
        return tareaRepository
                .buscarConFiltros(estado, categoria, prioridad, asignadoId, pageable)
                .map(this::toResponse);
    }

    // =====================================================================
    // Metodos auxiliares privados
    // =====================================================================

    /**
     * Metodo generico para transiciones simples (iniciar, revision, completar).
     * Usa una interfaz funcional ({@code Consumer<Tarea>}) para ejecutar
     * el metodo de dominio correspondiente sin duplicar codigo.
     */
    private TareaResponse transicionar(UUID tareaId, Integer version, UUID actorId,
                                        String accion, String observacion,
                                        java.util.function.Consumer<Tarea> transicion) {
        Tarea tarea = obtenerTareaConVersion(tareaId, version);
        Usuario actor = obtenerUsuario(actorId);

        transicion.accept(tarea);

        HistorialTarea historial = HistorialTarea.builder()
                .accion(accion)
                .actor(actor)
                .observacion(observacion)
                .fechaEvento(LocalDateTime.now())
                .build();
        tarea.agregarHistorial(historial);

        tarea = tareaRepository.save(tarea);
        return toResponseConHistorial(tarea);
    }

    private Tarea obtenerTareaConVersion(UUID tareaId, Integer versionCliente) {
        Tarea tarea = tareaRepository.findByIdConRelaciones(tareaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tarea", tareaId));

        if (!tarea.getVersion().equals(versionCliente)) {
            throw new ReglaNegocioException(
                    "Conflicto de concurrencia: version esperada " + versionCliente
                            + ", version actual " + tarea.getVersion());
        }
        return tarea;
    }

    private Usuario obtenerUsuario(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    // --- Mapeo entidad → DTO ---

    private TareaResponse toResponse(Tarea tarea) {
        return TareaResponse.builder()
                .id(tarea.getId())
                .titulo(tarea.getTitulo())
                .descripcion(tarea.getDescripcion())
                .estado(tarea.getEstado())
                .categoria(tarea.getCategoria())
                .prioridad(tarea.getPrioridad())
                .observacionCierre(tarea.getObservacionCierre())
                .creadorId(tarea.getCreador().getId())
                .creadorNombre(tarea.getCreador().getNombre())
                .asignadoId(tarea.getAsignado() != null ? tarea.getAsignado().getId() : null)
                .asignadoNombre(tarea.getAsignado() != null ? tarea.getAsignado().getNombre() : null)
                .fechaCreacion(tarea.getFechaCreacion())
                .fechaLimite(tarea.getFechaLimite())
                .version(tarea.getVersion())
                .historial(Collections.emptyList())
                .build();
    }

    private TareaResponse toResponseConHistorial(Tarea tarea) {
        TareaResponse response = toResponse(tarea);
        List<HistorialResponse> historial = tarea.getHistorial().stream()
                .map(h -> HistorialResponse.builder()
                        .id(h.getId())
                        .fechaEvento(h.getFechaEvento())
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
