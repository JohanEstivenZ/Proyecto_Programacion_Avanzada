package co.edu.uniquindio.taskflow.config;

import co.edu.uniquindio.taskflow.repository.TareaRepository;
import co.edu.uniquindio.taskflow.service.IAService;
import co.edu.uniquindio.taskflow.service.impl.IAServiceFallbackImpl;
import co.edu.uniquindio.taskflow.service.impl.IAServiceOpenAIImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion condicional del servicio de IA.
 *
 * <p>Segun la propiedad {@code ia.provider}:</p>
 * <ul>
 *   <li>{@code "openai"} → Usa Spring AI + ChatClient para llamar a OpenAI.</li>
 *   <li>Cualquier otro valor → Usa el fallback basado en reglas (sin red).</li>
 * </ul>
 *
 * <p>El metodo {@code @Bean} retorna una instancia de {@link IAService}.
 * Spring la inyecta en los controladores que la necesiten. Esta tecnica
 * se llama <strong>Factory Method con configuracion condicional</strong>.</p>
 */
@Slf4j
@Configuration
public class IAServiceConfig {

    @Value("${ia.provider:fallback}")
    private String iaProvider;

    @Bean
    public IAService iaService(TareaRepository tareaRepository,
                                ChatClient.Builder chatClientBuilder) {
        IAServiceFallbackImpl fallback = new IAServiceFallbackImpl(tareaRepository);

        if ("openai".equalsIgnoreCase(iaProvider)) {
            try {
                ChatClient chatClient = chatClientBuilder.build();
                log.info("IA Provider: OpenAI (Spring AI). Fallback activo si falla.");
                return new IAServiceOpenAIImpl(chatClient, tareaRepository, fallback);
            } catch (Exception e) {
                log.warn("No se pudo inicializar OpenAI: {}. Usando fallback.", e.getMessage());
                return fallback;
            }
        }

        log.info("IA Provider: FALLBACK (basado en reglas). Para OpenAI, configurar ia.provider=openai");
        return fallback;
    }
}
