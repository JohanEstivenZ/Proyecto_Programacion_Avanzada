package co.edu.uniquindio.taskflow.controller;

import co.edu.uniquindio.taskflow.dto.request.ResumenIARequest;
import co.edu.uniquindio.taskflow.dto.request.SugerenciaIARequest;
import co.edu.uniquindio.taskflow.dto.response.ResumenIAResponse;
import co.edu.uniquindio.taskflow.dto.response.SugerenciaIAResponse;
import co.edu.uniquindio.taskflow.service.IAService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la integracion con IA.
 *
 * <p>El try-catch garantiza que una falla de IA retorna 503 (Service Unavailable)
 * en vez de 500. El frontend puede mostrar "Servicio de IA no disponible" y
 * el usuario puede clasificar manualmente sin que el sistema se rompa.</p>
 */
@RestController
@RequestMapping("/ia")
@RequiredArgsConstructor
@Tag(name = "IA", description = "Sugerencias y resumenes con inteligencia artificial")
public class IAController {

    private final IAService iaService;

    @PostMapping("/sugerencias/clasificacion")
    @Operation(summary = "Sugerir categoria y prioridad para una tarea")
    public ResponseEntity<SugerenciaIAResponse> sugerirClasificacion(
            @Valid @RequestBody SugerenciaIARequest request) {
        try {
            return ResponseEntity.ok(iaService.sugerirClasificacion(request.getDescripcion()));
        } catch (Exception e) {
            return ResponseEntity.status(503).build();
        }
    }

    @PostMapping("/resumen")
    @Operation(summary = "Generar resumen de una tarea con su historial")
    public ResponseEntity<ResumenIAResponse> generarResumen(
            @Valid @RequestBody ResumenIARequest request) {
        try {
            return ResponseEntity.ok(iaService.generarResumen(request.getTareaId()));
        } catch (Exception e) {
            return ResponseEntity.status(503).build();
        }
    }
}
