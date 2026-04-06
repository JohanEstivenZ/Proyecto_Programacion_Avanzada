package co.edu.uniquindio.taskflow.service;

import co.edu.uniquindio.taskflow.domain.enums.*;
import co.edu.uniquindio.taskflow.domain.model.*;
import co.edu.uniquindio.taskflow.dto.request.*;
import co.edu.uniquindio.taskflow.dto.response.TareaResponse;
import co.edu.uniquindio.taskflow.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.taskflow.exception.ReglaNegocioException;
import co.edu.uniquindio.taskflow.repository.TareaRepository;
import co.edu.uniquindio.taskflow.repository.UsuarioRepository;
import co.edu.uniquindio.taskflow.service.impl.TareaServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TareaService — Tests unitarios")
class TareaServiceTest {

    @Mock private TareaRepository tareaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private TareaServiceImpl tareaService;

    private Usuario developer;
    private Usuario teamLead;
    private Tarea tareaPendiente;
    private Tarea tareaEnProgreso;

    @BeforeEach
    void setUp() {
        developer = Usuario.builder().id(UUID.randomUUID()).nombre("Ana Torres")
                .email("ana@test.com").rol(Rol.DEVELOPER).activo(true).build();
        teamLead = Usuario.builder().id(UUID.randomUUID()).nombre("Pedro Gomez")
                .email("pedro@test.com").rol(Rol.TEAM_LEAD).activo(true).build();

        tareaPendiente = Tarea.builder().id(UUID.randomUUID()).titulo("Tarea test")
                .descripcion("Descripcion de prueba").estado(EstadoTarea.PENDIENTE)
                .creador(developer).fechaCreacion(LocalDateTime.now())
                .version(0).historial(new ArrayList<>()).build();

        tareaEnProgreso = Tarea.builder().id(UUID.randomUUID()).titulo("Tarea en progreso")
                .descripcion("Ya iniciada").estado(EstadoTarea.EN_PROGRESO)
                .creador(developer).fechaCreacion(LocalDateTime.now())
                .version(1).historial(new ArrayList<>()).build();
    }

    @Nested
    @DisplayName("Crear tarea")
    class CrearTests {
        @Test
        @DisplayName("Debe crear tarea en estado PENDIENTE")
        void crear_exitoso() {
            TareaCrearRequest req = TareaCrearRequest.builder()
                    .titulo("Nueva tarea de prueba").descripcion("Descripcion valida para la nueva tarea").build();
            when(usuarioRepository.findById(developer.getId())).thenReturn(Optional.of(developer));
            when(tareaRepository.save(any(Tarea.class))).thenAnswer(inv -> {
                Tarea t = inv.getArgument(0); t.setId(UUID.randomUUID()); return t;
            });

            TareaResponse res = tareaService.crear(req, developer.getId());

            assertThat(res).isNotNull();
            assertThat(res.getEstado()).isEqualTo(EstadoTarea.PENDIENTE);
            verify(tareaRepository).save(any(Tarea.class));
        }

        @Test
        @DisplayName("Debe fallar si el creador no existe")
        void crear_creadorNoExiste() {
            UUID fakeId = UUID.randomUUID();
            TareaCrearRequest req = TareaCrearRequest.builder()
                    .titulo("Titulo valido").descripcion("Descripcion valida con mas de diez caracteres").build();
            when(usuarioRepository.findById(fakeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> tareaService.crear(req, fakeId))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("Clasificar tarea")
    class ClasificarTests {
        @Test
        @DisplayName("Debe clasificar exitosamente")
        void clasificar_exitoso() {
            ClasificarTareaRequest req = ClasificarTareaRequest.builder()
                    .version(0).categoria(Categoria.BUG).prioridad(Prioridad.ALTA).build();
            when(tareaRepository.findByIdConRelaciones(tareaPendiente.getId()))
                    .thenReturn(Optional.of(tareaPendiente));
            when(usuarioRepository.findById(teamLead.getId())).thenReturn(Optional.of(teamLead));
            when(tareaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TareaResponse res = tareaService.clasificar(tareaPendiente.getId(), req, teamLead.getId());
            assertThat(res.getCategoria()).isEqualTo(Categoria.BUG);
            assertThat(res.getPrioridad()).isEqualTo(Prioridad.ALTA);
        }

        @Test
        @DisplayName("Debe fallar por conflicto de version")
        void clasificar_versionConflicto() {
            ClasificarTareaRequest req = ClasificarTareaRequest.builder()
                    .version(99).categoria(Categoria.FEATURE).prioridad(Prioridad.MEDIA).build();
            when(tareaRepository.findByIdConRelaciones(tareaPendiente.getId()))
                    .thenReturn(Optional.of(tareaPendiente));

            assertThatThrownBy(() -> tareaService.clasificar(tareaPendiente.getId(), req, teamLead.getId()))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("concurrencia");
        }
    }

    @Nested
    @DisplayName("Transiciones de estado")
    class TransicionTests {
        @Test
        @DisplayName("PENDIENTE → EN_PROGRESO")
        void iniciar_exitoso() {
            VersionRequest req = VersionRequest.builder().version(0).build();
            when(tareaRepository.findByIdConRelaciones(tareaPendiente.getId()))
                    .thenReturn(Optional.of(tareaPendiente));
            when(usuarioRepository.findById(developer.getId())).thenReturn(Optional.of(developer));
            when(tareaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TareaResponse res = tareaService.iniciar(tareaPendiente.getId(), req, developer.getId());
            assertThat(res.getEstado()).isEqualTo(EstadoTarea.EN_PROGRESO);
        }

        @Test
        @DisplayName("No permite PENDIENTE → EN_REVISION (salto invalido)")
        void revision_desdePendiente_falla() {
            VersionRequest req = VersionRequest.builder().version(0).build();
            when(tareaRepository.findByIdConRelaciones(tareaPendiente.getId()))
                    .thenReturn(Optional.of(tareaPendiente));
            when(usuarioRepository.findById(developer.getId())).thenReturn(Optional.of(developer));

            assertThatThrownBy(() -> tareaService.enviarARevision(tareaPendiente.getId(), req, developer.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Archivar tarea")
    class ArchivarTests {
        @Test
        @DisplayName("Debe archivar tarea COMPLETADA con observacion")
        void archivar_exitoso() {
            Tarea completada = Tarea.builder().id(UUID.randomUUID()).titulo("Completada")
                    .descripcion("Ok").estado(EstadoTarea.COMPLETADA).creador(developer)
                    .fechaCreacion(LocalDateTime.now()).version(4).historial(new ArrayList<>()).build();
            ArchivarTareaRequest req = ArchivarTareaRequest.builder()
                    .version(4).observacion("Tarea terminada y verificada correctamente").build();

            when(tareaRepository.findByIdConRelaciones(completada.getId())).thenReturn(Optional.of(completada));
            when(usuarioRepository.findById(teamLead.getId())).thenReturn(Optional.of(teamLead));
            when(tareaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            TareaResponse res = tareaService.archivar(completada.getId(), req, teamLead.getId());
            assertThat(res.getEstado()).isEqualTo(EstadoTarea.ARCHIVADA);
        }

        @Test
        @DisplayName("Debe fallar con observacion corta")
        void archivar_observacionCorta() {
            Tarea completada = Tarea.builder().id(UUID.randomUUID()).titulo("Completada")
                    .descripcion("Ok").estado(EstadoTarea.COMPLETADA).creador(developer)
                    .fechaCreacion(LocalDateTime.now()).version(4).historial(new ArrayList<>()).build();
            ArchivarTareaRequest req = ArchivarTareaRequest.builder().version(4).observacion("corta").build();

            when(tareaRepository.findByIdConRelaciones(completada.getId())).thenReturn(Optional.of(completada));
            when(usuarioRepository.findById(teamLead.getId())).thenReturn(Optional.of(teamLead));

            assertThatThrownBy(() -> tareaService.archivar(completada.getId(), req, teamLead.getId()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
