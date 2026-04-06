package co.edu.uniquindio.taskflow.repository;

import co.edu.uniquindio.taskflow.domain.enums.*;
import co.edu.uniquindio.taskflow.domain.model.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TareaRepository — Tests de persistencia")
class TareaRepositoryTest {

    @Autowired private TestEntityManager em;
    @Autowired private TareaRepository tareaRepository;

    private Usuario dev;
    private Usuario lead;

    @BeforeEach
    void setUp() {
        dev = Usuario.builder().identificacion("3001").nombre("Ana Torres")
                .email("ana@test.com").password("hash").rol(Rol.DEVELOPER).activo(true).build();
        em.persistAndFlush(dev);
        lead = Usuario.builder().identificacion("2001").nombre("Pedro Gomez")
                .email("pedro@test.com").password("hash").rol(Rol.TEAM_LEAD).activo(true).build();
        em.persistAndFlush(lead);
    }

    private Tarea crearTarea(EstadoTarea estado, Categoria cat, Prioridad pri, Usuario asignado) {
        Tarea t = Tarea.builder().titulo("Tarea " + estado).descripcion("Desc")
                .estado(estado).categoria(cat).prioridad(pri).creador(dev)
                .asignado(asignado).fechaCreacion(LocalDateTime.now()).build();
        return em.persistAndFlush(t);
    }

    @Nested
    @DisplayName("CRUD basico")
    class CrudTests {
        @Test @DisplayName("Guardar y recuperar con UUID autogenerado")
        void guardarYRecuperar() {
            Tarea t = Tarea.builder().titulo("Test CRUD").descripcion("Desc test")
                    .estado(EstadoTarea.PENDIENTE).creador(dev).fechaCreacion(LocalDateTime.now()).build();
            Tarea saved = tareaRepository.save(t);
            em.flush(); em.clear();
            Optional<Tarea> found = tareaRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getTitulo()).isEqualTo("Test CRUD");
            assertThat(found.get().getVersion()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Queries JPQL")
    class QueryTests {
        @Test @DisplayName("findByIdConRelaciones carga creador y asignado")
        void findByIdConRelaciones() {
            Tarea t = crearTarea(EstadoTarea.EN_PROGRESO, Categoria.BUG, Prioridad.ALTA, lead);
            em.clear();
            Optional<Tarea> res = tareaRepository.findByIdConRelaciones(t.getId());
            assertThat(res).isPresent();
            assertThat(res.get().getCreador().getNombre()).isEqualTo("Ana Torres");
            assertThat(res.get().getAsignado().getNombre()).isEqualTo("Pedro Gomez");
        }

        @Test @DisplayName("findByIdConHistorial carga historial")
        void findByIdConHistorial() {
            Tarea t = crearTarea(EstadoTarea.PENDIENTE, null, null, null);
            HistorialTarea h = HistorialTarea.builder().accion("CREADA").actor(dev)
                    .observacion("Test").fechaEvento(LocalDateTime.now()).tarea(t).build();
            em.persistAndFlush(h);
            em.clear();
            Optional<Tarea> res = tareaRepository.findByIdConHistorial(t.getId());
            assertThat(res).isPresent();
            assertThat(res.get().getHistorial()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Filtros dinamicos")
    class FiltroTests {
        @BeforeEach void datos() {
            crearTarea(EstadoTarea.PENDIENTE, null, null, null);
            crearTarea(EstadoTarea.EN_PROGRESO, Categoria.BUG, Prioridad.URGENTE, lead);
            crearTarea(EstadoTarea.EN_PROGRESO, Categoria.FEATURE, Prioridad.MEDIA, lead);
            crearTarea(EstadoTarea.COMPLETADA, Categoria.MEJORA, Prioridad.BAJA, null);
        }

        @Test @DisplayName("Sin filtros retorna todos")
        void sinFiltros() {
            Page<Tarea> res = tareaRepository.buscarConFiltros(null, null, null, null, PageRequest.of(0, 20));
            assertThat(res.getTotalElements()).isEqualTo(4);
        }

        @Test @DisplayName("Filtrar por estado")
        void filtrarEstado() {
            Page<Tarea> res = tareaRepository.buscarConFiltros(EstadoTarea.EN_PROGRESO, null, null, null, PageRequest.of(0, 20));
            assertThat(res.getTotalElements()).isEqualTo(2);
        }

        @Test @DisplayName("Filtrar por categoria")
        void filtrarCategoria() {
            Page<Tarea> res = tareaRepository.buscarConFiltros(null, Categoria.BUG, null, null, PageRequest.of(0, 20));
            assertThat(res.getTotalElements()).isEqualTo(1);
        }

        @Test @DisplayName("Filtrar por asignado")
        void filtrarAsignado() {
            Page<Tarea> res = tareaRepository.buscarConFiltros(null, null, null, lead.getId(), PageRequest.of(0, 20));
            assertThat(res.getTotalElements()).isEqualTo(2);
        }

        @Test @DisplayName("Paginacion correcta")
        void paginacion() {
            Page<Tarea> res = tareaRepository.buscarConFiltros(null, null, null, null, PageRequest.of(0, 2));
            assertThat(res.getContent()).hasSize(2);
            assertThat(res.getTotalPages()).isEqualTo(2);
        }
    }
}
