package co.edu.uniquindio.taskflow.service.impl;

import co.edu.uniquindio.taskflow.domain.enums.Categoria;
import co.edu.uniquindio.taskflow.domain.enums.Prioridad;
import co.edu.uniquindio.taskflow.domain.model.HistorialTarea;
import co.edu.uniquindio.taskflow.domain.model.Tarea;
import co.edu.uniquindio.taskflow.dto.response.ResumenIAResponse;
import co.edu.uniquindio.taskflow.dto.response.SugerenciaIAResponse;
import co.edu.uniquindio.taskflow.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.taskflow.repository.TareaRepository;
import co.edu.uniquindio.taskflow.service.IAService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

/**
 * Implementacion de IA basada en reglas de palabras clave.
 * Funciona sin conexion a internet ni API key. Garantiza que
 * el sistema opera correctamente sin dependencia de servicios externos.
 */
@Slf4j
@RequiredArgsConstructor
public class IAServiceFallbackImpl implements IAService {

    private final TareaRepository tareaRepository;

    private static final Map<String, Categoria> KEYWORDS_CATEGORIA = Map.ofEntries(
            Map.entry("bug", Categoria.BUG),
            Map.entry("error", Categoria.BUG),
            Map.entry("fallo", Categoria.BUG),
            Map.entry("crash", Categoria.BUG),
            Map.entry("feature", Categoria.FEATURE),
            Map.entry("nueva funcionalidad", Categoria.FEATURE),
            Map.entry("implementar", Categoria.FEATURE),
            Map.entry("agregar", Categoria.FEATURE),
            Map.entry("refactorizar", Categoria.MEJORA),
            Map.entry("optimizar", Categoria.MEJORA),
            Map.entry("rendimiento", Categoria.MEJORA),
            Map.entry("documentar", Categoria.DOCUMENTACION),
            Map.entry("readme", Categoria.DOCUMENTACION),
            Map.entry("manual", Categoria.DOCUMENTACION),
            Map.entry("investigar", Categoria.INVESTIGACION),
            Map.entry("spike", Categoria.INVESTIGACION),
            Map.entry("evaluar", Categoria.INVESTIGACION)
    );

    private static final Map<String, Prioridad> KEYWORDS_PRIORIDAD = Map.of(
            "urgente", Prioridad.URGENTE,
            "critico", Prioridad.URGENTE,
            "produccion", Prioridad.URGENTE,
            "bloqueante", Prioridad.ALTA,
            "importante", Prioridad.ALTA,
            "deadline", Prioridad.ALTA
    );

    @Override
    public SugerenciaIAResponse sugerirClasificacion(String descripcion) {
        log.info("Sugerencia de clasificacion por REGLAS (fallback)");
        String texto = descripcion.toLowerCase().trim();

        Categoria categoria = null;
        Prioridad prioridad = Prioridad.MEDIA;
        float confianza = 0.0f;
        StringBuilder explicacion = new StringBuilder();

        for (Map.Entry<String, Categoria> entry : KEYWORDS_CATEGORIA.entrySet()) {
            if (texto.contains(entry.getKey())) {
                categoria = entry.getValue();
                confianza = 0.7f;
                explicacion.append("Palabra clave '").append(entry.getKey())
                        .append("' sugiere ").append(entry.getValue()).append(". ");
                break;
            }
        }

        for (Map.Entry<String, Prioridad> entry : KEYWORDS_PRIORIDAD.entrySet()) {
            if (texto.contains(entry.getKey())) {
                prioridad = entry.getValue();
                confianza = Math.min(confianza + 0.15f, 1.0f);
                explicacion.append("Palabra '").append(entry.getKey())
                        .append("' sugiere prioridad ").append(entry.getValue()).append(". ");
                break;
            }
        }

        if (categoria == null) {
            categoria = Categoria.FEATURE;
            confianza = 0.3f;
            explicacion.append("Sin palabras clave reconocidas. Se sugiere FEATURE por defecto.");
        }

        return SugerenciaIAResponse.builder()
                .categoriaSugerida(categoria)
                .prioridadSugerida(prioridad)
                .confianza(confianza)
                .explicacion("[Fallback] " + explicacion.toString().trim())
                .build();
    }

    @Override
    public ResumenIAResponse generarResumen(UUID tareaId) {
        log.info("Resumen por REGLAS (fallback) para tarea: {}", tareaId);

        Tarea tarea = tareaRepository.findByIdConHistorial(tareaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Tarea", tareaId));

        StringBuilder resumen = new StringBuilder();
        resumen.append("Tarea: ").append(tarea.getTitulo()).append("\n");
        resumen.append("Estado: ").append(tarea.getEstado()).append("\n");
        if (tarea.getCategoria() != null) resumen.append("Categoria: ").append(tarea.getCategoria()).append("\n");
        if (tarea.getPrioridad() != null) resumen.append("Prioridad: ").append(tarea.getPrioridad()).append("\n");
        if (tarea.getAsignado() != null) resumen.append("Asignado a: ").append(tarea.getAsignado().getNombre()).append("\n");

        resumen.append("\nHistorial (").append(tarea.getHistorial().size()).append(" eventos):\n");
        for (HistorialTarea h : tarea.getHistorial()) {
            resumen.append("  - ").append(h.getFechaEvento()).append(" | ")
                    .append(h.getAccion()).append(" | ").append(h.getActor().getNombre());
            if (h.getObservacion() != null) resumen.append(" | ").append(h.getObservacion());
            resumen.append("\n");
        }

        return ResumenIAResponse.builder()
                .resumen(resumen.toString())
                .generadoPor("Fallback (reglas)")
                .build();
    }
}
