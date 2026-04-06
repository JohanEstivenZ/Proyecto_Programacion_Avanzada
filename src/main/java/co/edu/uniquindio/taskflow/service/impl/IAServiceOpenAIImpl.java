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
import org.springframework.ai.chat.client.ChatClient;

import java.util.UUID;

/**
 * Implementacion con Spring AI + OpenAI.
 *
 * <p>{@link ChatClient} es la abstraccion de Spring AI para interactuar con LLMs.
 * Se configura automaticamente cuando {@code spring-ai-openai-spring-boot-starter}
 * esta en el classpath y la API key esta configurada.</p>
 *
 * <p>Si la llamada a OpenAI falla por cualquier razon (timeout, clave invalida,
 * rate limit), se delega automaticamente al {@link IAServiceFallbackImpl}.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class IAServiceOpenAIImpl implements IAService {

    private final ChatClient chatClient;
    private final TareaRepository tareaRepository;
    private final IAServiceFallbackImpl fallback;

    private static final String PROMPT_CLASIFICACION = """
            Eres un asistente de gestion de proyectos de software.
            
            Categorias posibles: BUG, FEATURE, MEJORA, DOCUMENTACION, INVESTIGACION
            Prioridades posibles: BAJA, MEDIA, ALTA, URGENTE
            
            Analiza esta descripcion de tarea y responde SOLO con JSON (sin markdown):
            {"categoriaSugerida":"NOMBRE","prioridadSugerida":"NOMBRE","confianza":0.85,"explicacion":"..."}
            
            Descripcion: %s
            """;

    private static final String PROMPT_RESUMEN = """
            Genera un resumen ejecutivo en espanol de esta tarea de desarrollo:
            %s
            Incluye: estado actual, puntos clave del historial y proximos pasos recomendados.
            """;

    @Override
    public SugerenciaIAResponse sugerirClasificacion(String descripcion) {
        log.info("Sugerencia de clasificacion con OpenAI");
        try {
            String respuesta = chatClient.prompt()
                    .user(String.format(PROMPT_CLASIFICACION, descripcion))
                    .call()
                    .content();

            return parsearRespuesta(respuesta);
        } catch (Exception e) {
            log.warn("OpenAI fallo, usando fallback: {}", e.getMessage());
            return fallback.sugerirClasificacion(descripcion);
        }
    }

    @Override
    public ResumenIAResponse generarResumen(UUID tareaId) {
        log.info("Resumen con OpenAI para tarea: {}", tareaId);
        try {
            Tarea tarea = tareaRepository.findByIdConHistorial(tareaId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Tarea", tareaId));

            String datos = construirDatos(tarea);
            String resumen = chatClient.prompt()
                    .user(String.format(PROMPT_RESUMEN, datos))
                    .call()
                    .content();

            return ResumenIAResponse.builder()
                    .resumen(resumen)
                    .generadoPor("OpenAI (Spring AI)")
                    .build();
        } catch (RecursoNoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            log.warn("OpenAI fallo para resumen, usando fallback: {}", e.getMessage());
            return fallback.generarResumen(tareaId);
        }
    }

    private SugerenciaIAResponse parsearRespuesta(String respuesta) {
        try {
            String json = respuesta.trim().replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            String cat = extraerCampo(json, "categoriaSugerida");
            String pri = extraerCampo(json, "prioridadSugerida");
            String conf = extraerCampo(json, "confianza");
            String exp = extraerCampo(json, "explicacion");

            Categoria categoria;
            try { categoria = Categoria.valueOf(cat); } catch (Exception e) { categoria = Categoria.FEATURE; }

            Prioridad prioridad;
            try { prioridad = Prioridad.valueOf(pri); } catch (Exception e) { prioridad = Prioridad.MEDIA; }

            float confianza;
            try { confianza = Float.parseFloat(conf); } catch (Exception e) { confianza = 0.7f; }

            return SugerenciaIAResponse.builder()
                    .categoriaSugerida(categoria)
                    .prioridadSugerida(prioridad)
                    .confianza(confianza)
                    .explicacion(exp != null ? exp : "Clasificacion generada por IA")
                    .build();
        } catch (Exception e) {
            log.warn("Error parseando respuesta de OpenAI: {}", e.getMessage());
            return SugerenciaIAResponse.builder()
                    .categoriaSugerida(Categoria.FEATURE)
                    .prioridadSugerida(Prioridad.MEDIA)
                    .confianza(0.5f)
                    .explicacion("No se pudo interpretar la respuesta del modelo.")
                    .build();
        }
    }

    private String extraerCampo(String json, String campo) {
        try {
            int idx = json.indexOf("\"" + campo + "\"");
            if (idx == -1) return null;
            int colon = json.indexOf(":", idx);
            if (colon == -1) return null;
            String after = json.substring(colon + 1).trim();
            if (after.startsWith("\"")) {
                int end = after.indexOf("\"", 1);
                return end > 0 ? after.substring(1, end) : null;
            } else {
                int end = after.indexOf(",");
                if (end == -1) end = after.indexOf("}");
                return end > 0 ? after.substring(0, end).trim() : after.trim();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private String construirDatos(Tarea tarea) {
        StringBuilder sb = new StringBuilder();
        sb.append("Titulo: ").append(tarea.getTitulo()).append("\n");
        sb.append("Descripcion: ").append(tarea.getDescripcion()).append("\n");
        sb.append("Estado: ").append(tarea.getEstado()).append("\n");
        if (tarea.getCategoria() != null) sb.append("Categoria: ").append(tarea.getCategoria()).append("\n");
        if (tarea.getPrioridad() != null) sb.append("Prioridad: ").append(tarea.getPrioridad()).append("\n");
        if (tarea.getAsignado() != null) sb.append("Asignado a: ").append(tarea.getAsignado().getNombre()).append("\n");
        sb.append("\nHistorial:\n");
        for (HistorialTarea h : tarea.getHistorial()) {
            sb.append("- ").append(h.getFechaEvento()).append(" | ").append(h.getAccion());
            if (h.getObservacion() != null) sb.append(" | ").append(h.getObservacion());
            sb.append("\n");
        }
        return sb.toString();
    }
}
