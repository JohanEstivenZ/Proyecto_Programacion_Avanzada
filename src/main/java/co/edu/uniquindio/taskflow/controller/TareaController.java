package co.edu.uniquindio.taskflow.controller;

import co.edu.uniquindio.taskflow.domain.enums.*;
import co.edu.uniquindio.taskflow.dto.request.*;
import co.edu.uniquindio.taskflow.dto.response.TareaResponse;
import co.edu.uniquindio.taskflow.service.TareaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para gestion de tareas y transiciones de estado.
 *
 * <h3>Verbos HTTP usados</h3>
 * <ul>
 *   <li>{@code POST} — Crear un recurso nuevo.</li>
 *   <li>{@code GET} — Consultar (sin modificar datos).</li>
 *   <li>{@code PATCH} — Modificar parcialmente un recurso.
 *       Usamos PATCH (no PUT) porque cada endpoint cambia solo un aspecto
 *       de la tarea (estado, asignado, categoria), no la reemplaza entera.</li>
 * </ul>
 *
 * <h3>{@code Authentication}</h3>
 * <p>Spring inyecta este objeto automaticamente. Contiene el usuario autenticado
 * que fue procesado por el {@code JwtAuthenticationFilter}.
 * {@code getPrincipal()} retorna el UUID del usuario (lo pusimos ahi en el filtro).</p>
 *
 * <h3>{@code @RequestParam(required = false)}</h3>
 * <p>Parametro de query string opcional. Si no se envia, llega como null.
 * Esto permite que el mismo endpoint sirva para buscar con o sin filtros.</p>
 */
@RestController
@RequestMapping("/tareas")
@RequiredArgsConstructor
@Tag(name = "Tareas", description = "CRUD y transiciones de estado de tareas")
public class TareaController {

    private final TareaService tareaService;

    @PostMapping
    @Operation(summary = "Crear nueva tarea")
    public ResponseEntity<TareaResponse> crear(
            @Valid @RequestBody TareaCrearRequest request,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tareaService.crear(request, getUserId(auth)));
    }

    @GetMapping
    @Operation(summary = "Listar tareas con filtros paginados")
    public ResponseEntity<Page<TareaResponse>> listar(
            @RequestParam(required = false) EstadoTarea estado,
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) UUID asignadoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(tareaService.listar(estado, categoria, prioridad, asignadoId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fechaCreacion"))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tarea por ID con historial")
    public ResponseEntity<TareaResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(tareaService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/clasificar")
    @Operation(summary = "Clasificar tarea (categoria + prioridad)")
    public ResponseEntity<TareaResponse> clasificar(
            @PathVariable UUID id, @Valid @RequestBody ClasificarTareaRequest request, Authentication auth) {
        return ResponseEntity.ok(tareaService.clasificar(id, request, getUserId(auth)));
    }

    @PatchMapping("/{id}/asignar")
    @Operation(summary = "Asignar responsable")
    public ResponseEntity<TareaResponse> asignar(
            @PathVariable UUID id, @Valid @RequestBody AsignarTareaRequest request, Authentication auth) {
        return ResponseEntity.ok(tareaService.asignar(id, request, getUserId(auth)));
    }

    @PatchMapping("/{id}/iniciar")
    @Operation(summary = "PENDIENTE → EN_PROGRESO")
    public ResponseEntity<TareaResponse> iniciar(
            @PathVariable UUID id, @Valid @RequestBody VersionRequest request, Authentication auth) {
        return ResponseEntity.ok(tareaService.iniciar(id, request, getUserId(auth)));
    }

    @PatchMapping("/{id}/revision")
    @Operation(summary = "EN_PROGRESO → EN_REVISION")
    public ResponseEntity<TareaResponse> revision(
            @PathVariable UUID id, @Valid @RequestBody VersionRequest request, Authentication auth) {
        return ResponseEntity.ok(tareaService.enviarARevision(id, request, getUserId(auth)));
    }

    @PatchMapping("/{id}/completar")
    @Operation(summary = "EN_REVISION → COMPLETADA")
    public ResponseEntity<TareaResponse> completar(
            @PathVariable UUID id, @Valid @RequestBody VersionRequest request, Authentication auth) {
        return ResponseEntity.ok(tareaService.completar(id, request, getUserId(auth)));
    }

    @PatchMapping("/{id}/archivar")
    @Operation(summary = "COMPLETADA → ARCHIVADA (con observacion)")
    public ResponseEntity<TareaResponse> archivar(
            @PathVariable UUID id, @Valid @RequestBody ArchivarTareaRequest request, Authentication auth) {
        return ResponseEntity.ok(tareaService.archivar(id, request, getUserId(auth)));
    }

    private UUID getUserId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }
}
