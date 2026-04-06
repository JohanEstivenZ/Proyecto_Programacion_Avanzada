package co.edu.uniquindio.taskflow.service;

import co.edu.uniquindio.taskflow.dto.response.ResumenIAResponse;
import co.edu.uniquindio.taskflow.dto.response.SugerenciaIAResponse;
import java.util.UUID;

/**
 * Contrato del servicio de IA. Tiene dos implementaciones:
 * {@code IAServiceFallbackImpl} (reglas) y {@code IAServiceOpenAIImpl} (Spring AI).
 */
public interface IAService {
    SugerenciaIAResponse sugerirClasificacion(String descripcion);
    ResumenIAResponse generarResumen(UUID tareaId);
}
