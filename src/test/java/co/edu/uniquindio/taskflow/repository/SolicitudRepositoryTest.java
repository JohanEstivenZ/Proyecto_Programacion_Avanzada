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
@DisplayName("SolicitudRepository — Tests de persistencia por RF")
class SolicitudRepositoryTest {

    @Autowired private TestEntityManager em;
    @Autowired private SolicitudRepository solicitudRepository;

    private Usuario estudiante;
    private Usuario responsable;

    @BeforeEach
    void setUp() {
        estudiante = em.persistAndFlush(Usuario.builder()
                .nombre("Ana Torres").email("ana@test.com")
                .password("hash").rol(RolUsuario.ESTUDIANTE).activo(true).build());

        responsable = em.persistAndFlush(Usuario.builder()
                .nombre("Pedro Gomez").email("pedro@test.com")
                .password("hash").rol(RolUsuario.RESPONSABLE).activo(true).build());
    }

    private Solicitud crearSolicitud(EstadoSolicitud estado, TipoSolicitud tipo,
                                     Prioridad prioridad, Usuario resp) {
        return em.persistAndFlush(Solicitud.builder()
                .tipo(tipo != null ? tipo : TipoSolicitud.CONSULTA_ACADEMICA)
                .descripcion("Descripción de prueba para el test")
                .canalOrigen(CanalOrigen.CORREO)
                .fechaRegistro(LocalDateTime.now())
                .estado(estado)
                .prioridad(prioridad)
                .solicitante(estudiante)
                .responsable(resp)
                .build());
    }


    // RF-01 — Registro de solicitudes


    @Nested
    @DisplayName("RF-01: Registro de solicitudes")
    class RF01 {

        @Test
        @DisplayName("Debe persistir solicitud con UUID autogenerado y estado REGISTRADA")
        void guardar_solicitudRegistrada() {
            Solicitud s = Solicitud.builder()
                    .tipo(TipoSolicitud.CONSULTA_ACADEMICA)
                    .descripcion("Consulta sobre el pensum académico")
                    .canalOrigen(CanalOrigen.CORREO)
                    .fechaRegistro(LocalDateTime.now())
                    .estado(EstadoSolicitud.REGISTRADA)
                    .solicitante(estudiante)
                    .build();

            Solicitud saved = solicitudRepository.save(s);
            em.flush(); em.clear();

            Optional<Solicitud> found = solicitudRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getEstado()).isEqualTo(EstadoSolicitud.REGISTRADA);
            assertThat(found.get().getId()).isNotNull();
            assertThat(found.get().getVersion()).isEqualTo(0);
        }

        @Test
        @DisplayName("Debe cargar solicitante con findByIdConRelaciones (RF-01)")
        void findByIdConRelaciones_cargaSolicitante() {
            Solicitud s = crearSolicitud(EstadoSolicitud.REGISTRADA,
                    TipoSolicitud.HOMOLOGACION, null, null);
            em.clear();

            Optional<Solicitud> res = solicitudRepository.findByIdConRelaciones(s.getId());
            assertThat(res).isPresent();
            assertThat(res.get().getSolicitante().getNombre()).isEqualTo("Ana Torres");
            assertThat(res.get().getSolicitante().getEmail()).isEqualTo("ana@test.com");
        }
    }


    // RF-02 — Clasificación de solicitudes


    @Nested
    @DisplayName("RF-02: Clasificación de solicitudes")
    class RF02 {

        @Test
        @DisplayName("Debe persistir solicitud con tipo CLASIFICADA")
        void guardar_solicitudClasificada() {
            Solicitud s = crearSolicitud(EstadoSolicitud.CLASIFICADA,
                    TipoSolicitud.HOMOLOGACION, null, null);
            em.clear();

            Optional<Solicitud> found = solicitudRepository.findById(s.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getTipo()).isEqualTo(TipoSolicitud.HOMOLOGACION);
            assertThat(found.get().getEstado()).isEqualTo(EstadoSolicitud.CLASIFICADA);
        }

        @Test
        @DisplayName("Debe filtrar solicitudes por tipo específico")
        void filtrar_porTipo() {
            crearSolicitud(EstadoSolicitud.CLASIFICADA, TipoSolicitud.HOMOLOGACION, null, null);
            crearSolicitud(EstadoSolicitud.CLASIFICADA, TipoSolicitud.CANCELACION, null, null);

            Page<Solicitud> res = solicitudRepository.buscarConFiltros(
                    null, TipoSolicitud.HOMOLOGACION, null, null, PageRequest.of(0, 20));

            assertThat(res.getTotalElements()).isEqualTo(1);
            assertThat(res.getContent().get(0).getTipo()).isEqualTo(TipoSolicitud.HOMOLOGACION);
        }
    }


    // RF-03 — Priorización de solicitudes


    @Nested
    @DisplayName("RF-03: Priorización de solicitudes")
    class RF03 {

        @Test
        @DisplayName("Debe persistir solicitud con prioridad y justificación")
        void guardar_conPrioridad() {
            Solicitud s = Solicitud.builder()
                    .tipo(TipoSolicitud.REGISTRO_ASIGNATURA)
                    .descripcion("Registro urgente de asignatura")
                    .canalOrigen(CanalOrigen.CSU)
                    .fechaRegistro(LocalDateTime.now())
                    .estado(EstadoSolicitud.CLASIFICADA)
                    .prioridad(Prioridad.ALTA)
                    .justificacionPrioridad("Fecha límite de matrícula próxima")
                    .solicitante(estudiante)
                    .build();

            Solicitud saved = solicitudRepository.save(s);
            em.flush(); em.clear();

            Optional<Solicitud> found = solicitudRepository.findById(saved.getId());
            assertThat(found.get().getPrioridad()).isEqualTo(Prioridad.ALTA);
            assertThat(found.get().getJustificacionPrioridad())
                    .isEqualTo("Fecha límite de matrícula próxima");
        }

        @Test
        @DisplayName("Debe filtrar solicitudes por prioridad ALTA")
        void filtrar_porPrioridad() {
            crearSolicitud(EstadoSolicitud.CLASIFICADA, null, Prioridad.ALTA, null);
            crearSolicitud(EstadoSolicitud.CLASIFICADA, null, Prioridad.BAJA, null);
            crearSolicitud(EstadoSolicitud.CLASIFICADA, null, Prioridad.MEDIA, null);

            Page<Solicitud> res = solicitudRepository.buscarConFiltros(
                    null, null, Prioridad.ALTA, null, PageRequest.of(0, 20));

            assertThat(res.getTotalElements()).isEqualTo(1);
            assertThat(res.getContent().get(0).getPrioridad()).isEqualTo(Prioridad.ALTA);
        }
    }


    // RF-04 — Gestión del ciclo de vida


    @Nested
    @DisplayName("RF-04: Gestión del ciclo de vida")
    class RF04 {

        @Test
        @DisplayName("Debe persistir todos los estados del ciclo de vida")
        void persistir_todosLosEstados() {
            for (EstadoSolicitud estado : EstadoSolicitud.values()) {
                Solicitud s = crearSolicitud(estado, null, null, null);
                em.clear();
                Optional<Solicitud> found = solicitudRepository.findById(s.getId());
                assertThat(found).isPresent();
                assertThat(found.get().getEstado()).isEqualTo(estado);
            }
        }

        @Test
        @DisplayName("Debe filtrar solicitudes por estado EN_ATENCION")
        void filtrar_porEstado() {
            crearSolicitud(EstadoSolicitud.REGISTRADA, null, null, null);
            crearSolicitud(EstadoSolicitud.EN_ATENCION, null, null, responsable);
            crearSolicitud(EstadoSolicitud.EN_ATENCION, null, null, responsable);

            Page<Solicitud> res = solicitudRepository.buscarConFiltros(
                    EstadoSolicitud.EN_ATENCION, null, null, null, PageRequest.of(0, 20));

            assertThat(res.getTotalElements()).isEqualTo(2);
        }
    }


    // RF-05 — Asignación de responsables


    @Nested
    @DisplayName("RF-05: Asignación de responsables")
    class RF05 {

        @Test
        @DisplayName("Debe persistir solicitud con responsable asignado")
        void guardar_conResponsable() {
            Solicitud s = crearSolicitud(EstadoSolicitud.EN_ATENCION,
                    TipoSolicitud.HOMOLOGACION, Prioridad.MEDIA, responsable);
            em.clear();

            Optional<Solicitud> res = solicitudRepository.findByIdConRelaciones(s.getId());
            assertThat(res).isPresent();
            assertThat(res.get().getResponsable().getNombre()).isEqualTo("Pedro Gomez");
        }

        @Test
        @DisplayName("Debe filtrar solicitudes por responsable asignado")
        void filtrar_porResponsable() {
            crearSolicitud(EstadoSolicitud.EN_ATENCION, null, null, responsable);
            crearSolicitud(EstadoSolicitud.EN_ATENCION, null, null, responsable);
            crearSolicitud(EstadoSolicitud.REGISTRADA, null, null, null);

            Page<Solicitud> res = solicitudRepository.buscarConFiltros(
                    null, null, null, responsable.getId(), PageRequest.of(0, 20));

            assertThat(res.getTotalElements()).isEqualTo(2);
            assertThat(res.getContent()).allMatch(
                    sol -> sol.getResponsable().getId().equals(responsable.getId()));
        }
    }


    // RF-06 — Registro del historial


    @Nested
    @DisplayName("RF-06: Registro del historial")
    class RF06 {

        @Test
        @DisplayName("Debe persistir historial asociado a la solicitud")
        void guardar_conHistorial() {
            Solicitud s = crearSolicitud(EstadoSolicitud.REGISTRADA, null, null, null);

            HistorialSolicitud h = HistorialSolicitud.builder()
                    .accion("SOLICITUD_REGISTRADA")
                    .actor(estudiante)
                    .observacion("Solicitud creada correctamente")
                    .fechaHora(LocalDateTime.now())
                    .solicitud(s)
                    .build();
            em.persistAndFlush(h);
            em.clear();

            Optional<Solicitud> res = solicitudRepository.findByIdConHistorial(s.getId());
            assertThat(res).isPresent();
            assertThat(res.get().getHistorial()).hasSize(1);
            assertThat(res.get().getHistorial().get(0).getAccion())
                    .isEqualTo("SOLICITUD_REGISTRADA");
        }

        @Test
        @DisplayName("Debe cargar múltiples entradas de historial en orden cronológico")
        void historial_ordenCronologico() {
            Solicitud s = crearSolicitud(EstadoSolicitud.EN_ATENCION, null, null, responsable);

            em.persistAndFlush(HistorialSolicitud.builder()
                    .accion("SOLICITUD_REGISTRADA").actor(estudiante)
                    .observacion("Creada").fechaHora(LocalDateTime.now().minusHours(2))
                    .solicitud(s).build());
            em.persistAndFlush(HistorialSolicitud.builder()
                    .accion("SOLICITUD_CLASIFICADA").actor(responsable)
                    .observacion("Clasificada").fechaHora(LocalDateTime.now().minusHours(1))
                    .solicitud(s).build());
            em.persistAndFlush(HistorialSolicitud.builder()
                    .accion("RESPONSABLE_ASIGNADO").actor(responsable)
                    .observacion("Asignada").fechaHora(LocalDateTime.now())
                    .solicitud(s).build());
            em.clear();

            Optional<Solicitud> res = solicitudRepository.findByIdConHistorial(s.getId());
            assertThat(res.get().getHistorial()).hasSize(3);
            assertThat(res.get().getHistorial().get(0).getAccion())
                    .isEqualTo("SOLICITUD_REGISTRADA");
            assertThat(res.get().getHistorial().get(2).getAccion())
                    .isEqualTo("RESPONSABLE_ASIGNADO");
        }
    }


    // RF-07 — Consulta de solicitudes


    @Nested
    @DisplayName("RF-07: Consulta de solicitudes")
    class RF07 {

        @BeforeEach
        void datos() {
            crearSolicitud(EstadoSolicitud.REGISTRADA, TipoSolicitud.CONSULTA_ACADEMICA, null, null);
            crearSolicitud(EstadoSolicitud.EN_ATENCION, TipoSolicitud.HOMOLOGACION, Prioridad.ALTA, responsable);
            crearSolicitud(EstadoSolicitud.EN_ATENCION, TipoSolicitud.CANCELACION, Prioridad.MEDIA, responsable);
            crearSolicitud(EstadoSolicitud.ATENDIDA, TipoSolicitud.REGISTRO_ASIGNATURA, Prioridad.BAJA, null);
        }

        @Test
        @DisplayName("Sin filtros retorna todas las solicitudes")
        void sinFiltros_retornaTodas() {
            Page<Solicitud> res = solicitudRepository.buscarConFiltros(
                    null, null, null, null, PageRequest.of(0, 20));
            assertThat(res.getTotalElements()).isEqualTo(4);
        }

        @Test
        @DisplayName("Paginación retorna el tamaño correcto")
        void paginacion_correcta() {
            Page<Solicitud> res = solicitudRepository.buscarConFiltros(
                    null, null, null, null, PageRequest.of(0, 2));
            assertThat(res.getContent()).hasSize(2);
            assertThat(res.getTotalPages()).isEqualTo(2);
        }
    }


    // RF-08 — Cierre de solicitudes


    @Nested
    @DisplayName("RF-08: Cierre de solicitudes")
    class RF08 {

        @Test
        @DisplayName("Debe persistir solicitud cerrada con observación")
        void guardar_solicitudCerrada() {
            Solicitud s = Solicitud.builder()
                    .tipo(TipoSolicitud.CANCELACION)
                    .descripcion("Cancelación procesada")
                    .canalOrigen(CanalOrigen.TELEFONICO)
                    .fechaRegistro(LocalDateTime.now())
                    .estado(EstadoSolicitud.CERRADA)
                    .observacionCierre("Solicitud atendida y verificada correctamente")
                    .solicitante(estudiante)
                    .responsable(responsable)
                    .build();

            Solicitud saved = solicitudRepository.save(s);
            em.flush(); em.clear();

            Optional<Solicitud> found = solicitudRepository.findById(saved.getId());
            assertThat(found.get().getEstado()).isEqualTo(EstadoSolicitud.CERRADA);
            assertThat(found.get().getObservacionCierre()).isNotBlank();
        }

        @Test
        @DisplayName("Solicitud cerrada no aparece en filtro de EN_ATENCION")
        void cerrada_noAparece_enFiltroActivas() {
            crearSolicitud(EstadoSolicitud.CERRADA, null, null, responsable);
            crearSolicitud(EstadoSolicitud.EN_ATENCION, null, null, responsable);

            Page<Solicitud> res = solicitudRepository.buscarConFiltros(
                    EstadoSolicitud.EN_ATENCION, null, null, null, PageRequest.of(0, 20));

            assertThat(res.getTotalElements()).isEqualTo(1);
            assertThat(res.getContent().get(0).getEstado())
                    .isEqualTo(EstadoSolicitud.EN_ATENCION);
        }
    }
}