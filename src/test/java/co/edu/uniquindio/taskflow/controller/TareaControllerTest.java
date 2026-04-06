package co.edu.uniquindio.taskflow.controller;

import co.edu.uniquindio.taskflow.config.JwtUtils;
import co.edu.uniquindio.taskflow.domain.enums.*;
import co.edu.uniquindio.taskflow.dto.request.TareaCrearRequest;
import co.edu.uniquindio.taskflow.dto.request.ClasificarTareaRequest;
import co.edu.uniquindio.taskflow.dto.response.TareaResponse;
import co.edu.uniquindio.taskflow.service.TareaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests del controlador de tareas.
 *
 * <p>Usamos {@code .with(authentication(...))} en vez de {@code @WithMockUser}
 * porque el controller hace {@code (UUID) auth.getPrincipal()}.
 * {@code @WithMockUser} pone un String como principal y causa ClassCastException.
 * Con {@code authentication()} controlamos que el principal sea un UUID real.</p>
 */
@WebMvcTest(TareaController.class)
@ActiveProfiles("test")
@DisplayName("TareaController — Tests de integracion web")
class TareaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private TareaService tareaService;
    @MockBean private JwtUtils jwtUtils;

    private static final UUID USER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private UsernamePasswordAuthenticationToken authToken(String rol) {
        return new UsernamePasswordAuthenticationToken(
                USER_ID, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
    }

    private TareaResponse mockResponse(EstadoTarea estado) {
        return TareaResponse.builder().id(UUID.randomUUID()).titulo("Test task")
                .descripcion("Test description").estado(estado).categoria(Categoria.FEATURE)
                .prioridad(Prioridad.MEDIA).creadorId(UUID.randomUUID()).creadorNombre("Ana")
                .fechaCreacion(LocalDateTime.now()).version(0).historial(Collections.emptyList()).build();
    }

    @Test
    @DisplayName("201 — Crear tarea exitosamente")
    void crear_exitoso() throws Exception {
        TareaCrearRequest req = TareaCrearRequest.builder()
                .titulo("Nueva tarea importante")
                .descripcion("Descripcion detallada de la tarea nueva").build();
        when(tareaService.crear(any(), any(UUID.class))).thenReturn(mockResponse(EstadoTarea.PENDIENTE));

        mockMvc.perform(post("/tareas")
                        .with(csrf())
                        .with(authentication(authToken("DEVELOPER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("400 — Titulo muy corto")
    void crear_tituloCorto() throws Exception {
        String json = "{\"titulo\":\"ab\",\"descripcion\":\"Descripcion valida con mas de diez chars\"}";
        mockMvc.perform(post("/tareas")
                        .with(csrf())
                        .with(authentication(authToken("DEVELOPER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.titulo").exists());
    }

    @Test
    @DisplayName("200 — Team Lead puede clasificar")
    void clasificar_teamLead_exitoso() throws Exception {
        UUID id = UUID.randomUUID();
        ClasificarTareaRequest req = ClasificarTareaRequest.builder()
                .version(0).categoria(Categoria.BUG).prioridad(Prioridad.URGENTE).build();
        TareaResponse resp = mockResponse(EstadoTarea.PENDIENTE);
        resp.setCategoria(Categoria.BUG);
        when(tareaService.clasificar(any(), any(), any())).thenReturn(resp);

        mockMvc.perform(patch("/tareas/{id}/clasificar", id)
                        .with(csrf())
                        .with(authentication(authToken("TEAM_LEAD")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoria").value("BUG"));
    }

    @Test
    @DisplayName("Sin autenticacion se rechaza la peticion")
    void crear_sinAuth() throws Exception {
        String json = "{\"titulo\":\"Titulo valido aqui\",\"descripcion\":\"Descripcion valida con longitud suficiente\"}";

        mockMvc.perform(post("/tareas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is4xxClientError());
    }
}