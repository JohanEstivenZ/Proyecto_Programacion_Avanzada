package co.edu.uniquindio.taskflow.controller;

import co.edu.uniquindio.taskflow.domain.enums.*;
import co.edu.uniquindio.taskflow.dto.request.*;
import co.edu.uniquindio.taskflow.dto.response.*;
import co.edu.uniquindio.taskflow.service.SolicitudService;
import co.edu.uniquindio.taskflow.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PostMapping
    public ResponseEntity<SolicitudResponse> crear(
            @Valid @RequestBody SolicitudCreacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = (userDetails != null) ? userDetails.getUsername() : "test@test.com";

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudService.crear(request, email));
    }

    @GetMapping
    public ResponseEntity<Page<SolicitudResponse>> listar(
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) TipoSolicitud tipo,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) UUID responsableId,
            Pageable pageable) {
        return ResponseEntity.ok(
                solicitudService.listar(estado, tipo, prioridad, responsableId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitudResponse> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(solicitudService.obtenerPorId(id));
    }

    @PutMapping("/{id}/clasificar")
    public ResponseEntity<SolicitudResponse> clasificar(
            @PathVariable UUID id,
            @Valid @RequestBody ClasificacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID actorId = obtenerActorId(userDetails);
        return ResponseEntity.ok(solicitudService.clasificar(id, request, actorId));
    }

    @PutMapping("/{id}/priorizar")
    public ResponseEntity<SolicitudResponse> priorizar(
            @PathVariable UUID id,
            @Valid @RequestBody PrioridadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID actorId = obtenerActorId(userDetails);
        return ResponseEntity.ok(solicitudService.priorizar(id, request, actorId));
    }

    @PutMapping("/{id}/asignar")
    public ResponseEntity<SolicitudResponse> asignar(
            @PathVariable UUID id,
            @Valid @RequestBody AsignacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID actorId = obtenerActorId(userDetails);
        return ResponseEntity.ok(solicitudService.asignarResponsable(id, request, actorId));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<SolicitudResponse> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CambioEstadoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID actorId = obtenerActorId(userDetails);
        return ResponseEntity.ok(solicitudService.cambiarEstado(id, request, actorId));
    }

    @PutMapping("/{id}/cerrar")
    public ResponseEntity<SolicitudResponse> cerrar(
            @PathVariable UUID id,
            @Valid @RequestBody CierreRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID actorId = obtenerActorId(userDetails);
        return ResponseEntity.ok(solicitudService.cerrar(id, request, actorId));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<SolicitudResponse> historial(@PathVariable UUID id) {
        return ResponseEntity.ok(solicitudService.obtenerPorId(id));
    }
    private final UsuarioService usuarioService;

    private UUID obtenerActorId(UserDetails userDetails) {
        if (userDetails == null) {
            return UUID.randomUUID();
        }
        return usuarioService.obtenerIdPorEmail(userDetails.getUsername());
    }
}