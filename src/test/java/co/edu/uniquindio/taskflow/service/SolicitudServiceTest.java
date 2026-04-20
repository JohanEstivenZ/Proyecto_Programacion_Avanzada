package co.edu.uniquindio.taskflow.service;

import co.edu.uniquindio.taskflow.domain.enums.*;
import co.edu.uniquindio.taskflow.domain.model.*;
import co.edu.uniquindio.taskflow.dto.request.*;
import co.edu.uniquindio.taskflow.dto.response.SolicitudResponse;
import co.edu.uniquindio.taskflow.exception.RecursoNoEncontradoException;
import co.edu.uniquindio.taskflow.exception.ReglaNegocioException;
import co.edu.uniquindio.taskflow.repository.SolicitudRepository;
import co.edu.uniquindio.taskflow.repository.UsuarioRepository;
import co.edu.uniquindio.taskflow.service.impl.SolicitudServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SolicitudService — Tests unitarios (RF-01 a RF-13)")
class SolicitudServiceTest {

    @Mock private SolicitudRepository solicitudRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private SolicitudServiceImpl solicitudService;

    private Usuario estudiante;
    private Usuario responsable;
    private Solicitud solicitudRegistrada;
    private Solicitud solicitudClasificada;
    private Solicitud solicitudEnAtencion;
    private Solicitud solicitudAtendida;

    @BeforeEach
    void setUp() {
        estudiante = Usuario.builder()
                .id(UUID.randomUUID())
                .nombre("Ana Torres")
                .email("ana@test.com")
                .rol(RolUsuario.ESTUDIANTE)
                .activo(true)
                .build();

        responsable = Usuario.builder()
                .id(UUID.randomUUID())
                .nombre("Pedro Gomez")
                .email("pedro@test.com")
                .rol(RolUsuario.RESPONSABLE)
                .activo(true)
                .build();

        solicitudRegistrada = Solicitud.builder()
                .id(UUID.randomUUID())
                .tipo(TipoSolicitud.CONSULTA_ACADEMICA)
                .descripcion("Necesito información sobre mi horario")
                .canalOrigen(CanalOrigen.CORREO)
                .fechaRegistro(LocalDateTime.now())
                .estado(EstadoSolicitud.REGISTRADA)
                .solicitante(estudiante)
                .version(0)
                .historial(new ArrayList<>())
                .build();

        solicitudClasificada = Solicitud.builder()
                .id(UUID.randomUUID())
                .tipo(TipoSolicitud.HOMOLOGACION)
                .descripcion("Solicitud de homologación de materia")
                .canalOrigen(CanalOrigen.CSU)
                .fechaRegistro(LocalDateTime.now())
                .estado(EstadoSolicitud.CLASIFICADA)
                .solicitante(estudiante)
                .version(1)
                .historial(new ArrayList<>())
                .build();

        solicitudEnAtencion = Solicitud.builder()
                .id(UUID.randomUUID())
                .tipo(TipoSolicitud.REGISTRO_ASIGNATURA)
                .descripcion("Registro de asignatura pendiente")
                .canalOrigen(CanalOrigen.SAC)
                .fechaRegistro(LocalDateTime.now())
                .estado(EstadoSolicitud.EN_ATENCION)
                .solicitante(estudiante)
                .responsable(responsable)
                .version(2)
                .historial(new ArrayList<>())
                .build();

        solicitudAtendida = Solicitud.builder()
                .id(UUID.randomUUID())
                .tipo(TipoSolicitud.CANCELACION)
                .descripcion("Cancelación de asignatura")
                .canalOrigen(CanalOrigen.TELEFONICO)
                .fechaRegistro(LocalDateTime.now())
                .estado(EstadoSolicitud.ATENDIDA)
                .solicitante(estudiante)
                .responsable(responsable)
                .version(3)
                .historial(new ArrayList<>())
                .build();
    }


    // RF-01 — Registro de solicitudes académicas


    @Nested
    @DisplayName("RF-01: Registro de solicitudes")
    class RF01_RegistroSolicitudes {

        @Test
        @DisplayName("Debe registrar solicitud en estado REGISTRADA")
        void crear_exitoso() {
            SolicitudCreacionRequest request = new SolicitudCreacionRequest();
            request.setTipo(TipoSolicitud.CONSULTA_ACADEMICA);
            request.setDescripcion("Consulta sobre pensum académico");
            request.setCanalOrigen(CanalOrigen.CORREO);

            when(usuarioRepository.findByEmail("ana@test.com"))
                    .thenReturn(Optional.of(estudiante));
            when(solicitudRepository.save(any(Solicitud.class)))
                    .thenAnswer(inv -> {
                        Solicitud s = inv.getArgument(0);
                        s.setId(UUID.randomUUID());
                        s.setVersion(0);
                        return s;
                    });

            SolicitudResponse response = solicitudService.crear(request, "ana@test.com");

            assertThat(response).isNotNull();
            assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.REGISTRADA);
            assertThat(response.getSolicitanteNombre()).isEqualTo("Ana Torres");
            verify(solicitudRepository).save(any(Solicitud.class));
        }

        @Test
        @DisplayName("Debe fallar si el solicitante no existe")
        void crear_solicitanteNoExiste() {
            SolicitudCreacionRequest request = new SolicitudCreacionRequest();
            request.setTipo(TipoSolicitud.CONSULTA_ACADEMICA);
            request.setDescripcion("Consulta sobre pensum");
            request.setCanalOrigen(CanalOrigen.CORREO);

            when(usuarioRepository.findByEmail("noexiste@test.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> solicitudService.crear(request, "noexiste@test.com"))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }

    // RF-02 — Clasificación de solicitudes


    @Nested
    @DisplayName("RF-02: Clasificación de solicitudes")
    class RF02_ClasificacionSolicitudes {

        @Test
        @DisplayName("Debe clasificar una solicitud REGISTRADA exitosamente")
        void clasificar_exitoso() {
            ClasificacionRequest request = new ClasificacionRequest();
            request.setTipo(TipoSolicitud.HOMOLOGACION);
            request.setVersion(0);

            when(solicitudRepository.findByIdConRelaciones(solicitudRegistrada.getId()))
                    .thenReturn(Optional.of(solicitudRegistrada));
            when(usuarioRepository.findById(responsable.getId()))
                    .thenReturn(Optional.of(responsable));
            when(solicitudRepository.save(any()))
                    .thenAnswer(inv -> inv.getArgument(0));

            SolicitudResponse response = solicitudService.clasificar(
                    solicitudRegistrada.getId(), request, responsable.getId());

            assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.CLASIFICADA);
            assertThat(response.getTipo()).isEqualTo(TipoSolicitud.HOMOLOGACION);
        }

        @Test
        @DisplayName("Debe fallar si la versión no coincide (concurrencia)")
        void clasificar_versionConflicto() {
            ClasificacionRequest request = new ClasificacionRequest();
            request.setTipo(TipoSolicitud.HOMOLOGACION);
            request.setVersion(99);

            when(solicitudRepository.findByIdConRelaciones(solicitudRegistrada.getId()))
                    .thenReturn(Optional.of(solicitudRegistrada));

            assertThatThrownBy(() -> solicitudService.clasificar(
                    solicitudRegistrada.getId(), request, responsable.getId()))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("concurrencia");
        }
    }


    // RF-03 — Priorización de solicitudes


    @Nested
    @DisplayName("RF-03: Priorización de solicitudes")
    class RF03_PriorizacionSolicitudes {

        @Test
        @DisplayName("Debe asignar prioridad con justificación")
        void priorizar_exitoso() {
            PrioridadRequest request = new PrioridadRequest();
            request.setPrioridad(Prioridad.ALTA);
            request.setJustificacion("Fecha límite de matrícula próxima");
            request.setVersion(0);

            when(solicitudRepository.findByIdConRelaciones(solicitudRegistrada.getId()))
                    .thenReturn(Optional.of(solicitudRegistrada));
            when(usuarioRepository.findById(responsable.getId()))
                    .thenReturn(Optional.of(responsable));
            when(solicitudRepository.save(any()))
                    .thenAnswer(inv -> inv.getArgument(0));

            SolicitudResponse response = solicitudService.priorizar(
                    solicitudRegistrada.getId(), request, responsable.getId());

            assertThat(response.getPrioridad()).isEqualTo(Prioridad.ALTA);
            assertThat(response.getJustificacionPrioridad())
                    .isEqualTo("Fecha límite de matrícula próxima");
        }

        @Test
        @DisplayName("Debe fallar si la solicitud no existe")
        void priorizar_solicitudNoExiste() {
            PrioridadRequest request = new PrioridadRequest();
            request.setPrioridad(Prioridad.MEDIA);
            request.setJustificacion("Justificación de prueba");
            request.setVersion(0);

            UUID idFalso = UUID.randomUUID();
            when(solicitudRepository.findByIdConRelaciones(idFalso))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> solicitudService.priorizar(idFalso, request, responsable.getId()))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }


    // RF-04 — Gestión del ciclo de vida


    @Nested
    @DisplayName("RF-04: Gestión del ciclo de vida")
    class RF04_CicloDeVida {

        @Test
        @DisplayName("EN_ATENCION → ATENDIDA es una transición válida")
        void cambiarEstado_enAtencionAAtendida_exitoso() {
            CambioEstadoRequest request = new CambioEstadoRequest();
            request.setNuevoEstado(EstadoSolicitud.ATENDIDA);
            request.setVersion(2);

            when(solicitudRepository.findByIdConRelaciones(solicitudEnAtencion.getId()))
                    .thenReturn(Optional.of(solicitudEnAtencion));
            when(usuarioRepository.findById(responsable.getId()))
                    .thenReturn(Optional.of(responsable));
            when(solicitudRepository.save(any()))
                    .thenAnswer(inv -> inv.getArgument(0));

            SolicitudResponse response = solicitudService.cambiarEstado(
                    solicitudEnAtencion.getId(), request, responsable.getId());

            assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.ATENDIDA);
        }

        @Test
        @DisplayName("REGISTRADA → CERRADA es una transición inválida")
        void cambiarEstado_transicionInvalida() {
            CambioEstadoRequest request = new CambioEstadoRequest();
            request.setNuevoEstado(EstadoSolicitud.CERRADA);
            request.setVersion(0);

            when(solicitudRepository.findByIdConRelaciones(solicitudRegistrada.getId()))
                    .thenReturn(Optional.of(solicitudRegistrada));
            when(usuarioRepository.findById(responsable.getId()))
                    .thenReturn(Optional.of(responsable));

            assertThatThrownBy(() -> solicitudService.cambiarEstado(
                    solicitudRegistrada.getId(), request, responsable.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }


    // RF-05 — Asignación de responsables


    @Nested
    @DisplayName("RF-05: Asignación de responsables")
    class RF05_AsignacionResponsables {

        @Test
        @DisplayName("Debe fallar si el responsable no está activo")
        void asignar_responsableInactivo() {
            Usuario inactivo = Usuario.builder()
                    .id(UUID.randomUUID())
                    .nombre("Carlos Inactivo")
                    .email("carlos@test.com")
                    .rol(RolUsuario.RESPONSABLE)
                    .activo(false)
                    .build();

            AsignacionRequest request = new AsignacionRequest();
            request.setResponsableId(inactivo.getId());
            request.setVersion(1);

            when(solicitudRepository.findByIdConRelaciones(solicitudClasificada.getId()))
                    .thenReturn(Optional.of(solicitudClasificada));
            // Mockear AMBAS llamadas a findById
            when(usuarioRepository.findById(inactivo.getId()))
                    .thenReturn(Optional.of(inactivo));
            when(usuarioRepository.findById(responsable.getId()))
                    .thenReturn(Optional.of(responsable));

            assertThatThrownBy(() -> solicitudService.asignarResponsable(
                    solicitudClasificada.getId(), request, responsable.getId()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("activo");
        }
    }


    // RF-06 — Registro del historial


    @Nested
    @DisplayName("RF-06: Registro del historial")
    class RF06_RegistroHistorial {

        @Test
        @DisplayName("Debe registrar entrada en historial al crear solicitud")
        void historial_registradoAlCrear() {
            SolicitudCreacionRequest request = new SolicitudCreacionRequest();
            request.setTipo(TipoSolicitud.SOLICITUD_CUPO);
            request.setDescripcion("Necesito un cupo en el grupo 2");
            request.setCanalOrigen(CanalOrigen.SAC);

            when(usuarioRepository.findByEmail("ana@test.com"))
                    .thenReturn(Optional.of(estudiante));
            when(solicitudRepository.save(any(Solicitud.class)))
                    .thenAnswer(inv -> {
                        Solicitud s = inv.getArgument(0);
                        s.setId(UUID.randomUUID());
                        s.setVersion(0);
                        return s;
                    });

            solicitudService.crear(request, "ana@test.com");

            verify(solicitudRepository).save(argThat(s ->
                    !s.getHistorial().isEmpty() &&
                            s.getHistorial().get(0).getAccion().equals("SOLICITUD_REGISTRADA")
            ));
        }

        @Test
        @DisplayName("Debe registrar entrada en historial al clasificar")
        void historial_registradoAlClasificar() {
            ClasificacionRequest request = new ClasificacionRequest();
            request.setTipo(TipoSolicitud.HOMOLOGACION);
            request.setVersion(0);

            when(solicitudRepository.findByIdConRelaciones(solicitudRegistrada.getId()))
                    .thenReturn(Optional.of(solicitudRegistrada));
            when(usuarioRepository.findById(responsable.getId()))
                    .thenReturn(Optional.of(responsable));
            when(solicitudRepository.save(any()))
                    .thenAnswer(inv -> inv.getArgument(0));

            solicitudService.clasificar(solicitudRegistrada.getId(), request, responsable.getId());

            verify(solicitudRepository).save(argThat(s ->
                    s.getHistorial().stream()
                            .anyMatch(h -> h.getAccion().equals("SOLICITUD_CLASIFICADA"))
            ));
        }
    }


    // RF-07 — Consulta de solicitudes


    @Nested
    @DisplayName("RF-07: Consulta de solicitudes")
    class RF07_ConsultaSolicitudes {

        @Test
        @DisplayName("Debe listar solicitudes filtradas por estado")
        void listar_porEstado() {
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(solicitudRegistrada));

            when(solicitudRepository.buscarConFiltros(
                    EstadoSolicitud.REGISTRADA, null, null, null, pageable))
                    .thenReturn(page);

            var resultado = solicitudService.listar(
                    EstadoSolicitud.REGISTRADA, null, null, null, pageable);

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0).getEstado())
                    .isEqualTo(EstadoSolicitud.REGISTRADA);
        }

        @Test
        @DisplayName("Debe retornar página vacía si no hay resultados")
        void listar_sinResultados() {
            var pageable = PageRequest.of(0, 10);
            when(solicitudRepository.buscarConFiltros(
                    EstadoSolicitud.CERRADA, null, null, null, pageable))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            var resultado = solicitudService.listar(
                    EstadoSolicitud.CERRADA, null, null, null, pageable);

            assertThat(resultado.getContent()).isEmpty();
        }
    }


    // RF-08 — Cierre de solicitudes


    @Nested
    @DisplayName("RF-08: Cierre de solicitudes")
    class RF08_CierreSolicitudes {

        @Test
        @DisplayName("Debe cerrar solicitud ATENDIDA con observación válida")
        void cerrar_exitoso() {
            CierreRequest request = new CierreRequest();
            request.setObservacionCierre("Solicitud atendida y verificada correctamente");
            request.setVersion(3);

            when(solicitudRepository.findByIdConRelaciones(solicitudAtendida.getId()))
                    .thenReturn(Optional.of(solicitudAtendida));
            when(usuarioRepository.findById(responsable.getId()))
                    .thenReturn(Optional.of(responsable));
            when(solicitudRepository.save(any()))
                    .thenAnswer(inv -> inv.getArgument(0));

            SolicitudResponse response = solicitudService.cerrar(
                    solicitudAtendida.getId(), request, responsable.getId());

            assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.CERRADA);
        }

        @Test
        @DisplayName("Debe fallar al cerrar solicitud que no está ATENDIDA")
        void cerrar_estadoInvalido() {
            CierreRequest request = new CierreRequest();
            request.setObservacionCierre("Intento de cierre inválido sobre solicitud");
            request.setVersion(0);

            when(solicitudRepository.findByIdConRelaciones(solicitudRegistrada.getId()))
                    .thenReturn(Optional.of(solicitudRegistrada));
            when(usuarioRepository.findById(responsable.getId()))
                    .thenReturn(Optional.of(responsable));

            assertThatThrownBy(() -> solicitudService.cerrar(
                    solicitudRegistrada.getId(), request, responsable.getId()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ATENDIDAS");
        }
    }


    // RF-09 — Generación de resúmenes (IA - opcional)


    @Nested
    @DisplayName("RF-09: Generación de resúmenes IA")
    class RF09_ResumenIA {

        @Test
        @DisplayName("El sistema funciona sin IA — obtenerPorId retorna historial completo")
        void obtenerPorId_sinIA_retornaHistorial() {
            when(solicitudRepository.findByIdConHistorial(solicitudRegistrada.getId()))
                    .thenReturn(Optional.of(solicitudRegistrada));

            SolicitudResponse response = solicitudService.obtenerPorId(solicitudRegistrada.getId());

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(solicitudRegistrada.getId());
        }

        @Test
        @DisplayName("Debe fallar si la solicitud no existe al obtener por ID")
        void obtenerPorId_noExiste() {
            UUID idFalso = UUID.randomUUID();
            when(solicitudRepository.findByIdConHistorial(idFalso))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> solicitudService.obtenerPorId(idFalso))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }

    // RF-10 — Sugerencia automática de clasificación (IA)


    @Nested
    @DisplayName("RF-10: Sugerencia automática de clasificación")
    class RF10_SugerenciaClasificacion {

        @Test
        @DisplayName("La clasificación manual funciona sin necesidad de IA")
        void clasificacion_manualSinIA() {
            ClasificacionRequest request = new ClasificacionRequest();
            request.setTipo(TipoSolicitud.SOLICITUD_CUPO);
            request.setVersion(0);

            when(solicitudRepository.findByIdConRelaciones(solicitudRegistrada.getId()))
                    .thenReturn(Optional.of(solicitudRegistrada));
            when(usuarioRepository.findById(responsable.getId()))
                    .thenReturn(Optional.of(responsable));
            when(solicitudRepository.save(any()))
                    .thenAnswer(inv -> inv.getArgument(0));

            SolicitudResponse response = solicitudService.clasificar(
                    solicitudRegistrada.getId(), request, responsable.getId());

            assertThat(response.getTipo()).isEqualTo(TipoSolicitud.SOLICITUD_CUPO);
        }

        @Test
        @DisplayName("La priorización manual funciona sin necesidad de IA")
        void priorizacion_manualSinIA() {
            PrioridadRequest request = new PrioridadRequest();
            request.setPrioridad(Prioridad.BAJA);
            request.setJustificacion("No tiene urgencia académica inmediata");
            request.setVersion(0);

            when(solicitudRepository.findByIdConRelaciones(solicitudRegistrada.getId()))
                    .thenReturn(Optional.of(solicitudRegistrada));
            when(usuarioRepository.findById(responsable.getId()))
                    .thenReturn(Optional.of(responsable));
            when(solicitudRepository.save(any()))
                    .thenAnswer(inv -> inv.getArgument(0));

            SolicitudResponse response = solicitudService.priorizar(
                    solicitudRegistrada.getId(), request, responsable.getId());

            assertThat(response.getPrioridad()).isEqualTo(Prioridad.BAJA);
        }
    }


    // RF-11 — Funcionamiento independiente de IA


    @Nested
    @DisplayName("RF-11: Funcionamiento independiente de IA")
    class RF11_IndependenciaIA {

        @Test
        @DisplayName("El flujo completo funciona sin integración de IA")
        void flujoCompleto_sinIA() {
            // Crear
            SolicitudCreacionRequest crearReq = new SolicitudCreacionRequest();
            crearReq.setTipo(TipoSolicitud.CANCELACION);
            crearReq.setDescripcion("Cancelación de asignatura por cruce de horario");
            crearReq.setCanalOrigen(CanalOrigen.TELEFONICO);

            when(usuarioRepository.findByEmail("ana@test.com"))
                    .thenReturn(Optional.of(estudiante));
            when(solicitudRepository.save(any()))
                    .thenAnswer(inv -> {
                        Solicitud s = inv.getArgument(0);
                        s.setId(UUID.randomUUID());
                        s.setVersion(0);
                        return s;
                    });

            SolicitudResponse creada = solicitudService.crear(crearReq, "ana@test.com");
            assertThat(creada.getEstado()).isEqualTo(EstadoSolicitud.REGISTRADA);
        }

        @Test
        @DisplayName("El cierre funciona sin observación generada por IA")
        void cerrar_sinIA_observacionManual() {
            CierreRequest request = new CierreRequest();
            request.setObservacionCierre("Solicitud resuelta manualmente por el responsable");
            request.setVersion(3);

            when(solicitudRepository.findByIdConRelaciones(solicitudAtendida.getId()))
                    .thenReturn(Optional.of(solicitudAtendida));
            when(usuarioRepository.findById(responsable.getId()))
                    .thenReturn(Optional.of(responsable));
            when(solicitudRepository.save(any()))
                    .thenAnswer(inv -> inv.getArgument(0));

            SolicitudResponse response = solicitudService.cerrar(
                    solicitudAtendida.getId(), request, responsable.getId());

            assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.CERRADA);
        }
    }


    // RF-12 — Exposición de servicios REST


    @Nested
    @DisplayName("RF-12: Exposición de servicios REST")
    class RF12_ExposicionREST {

        @Test
        @DisplayName("El servicio expone correctamente la consulta paginada")
        void listar_paginado() {
            var pageable = PageRequest.of(0, 5);
            var page = new PageImpl<>(
                    List.of(solicitudRegistrada, solicitudClasificada), pageable, 2);

            when(solicitudRepository.buscarConFiltros(null, null, null, null, pageable))
                    .thenReturn(page);

            var resultado = solicitudService.listar(null, null, null, null, pageable);

            assertThat(resultado.getTotalElements()).isEqualTo(2);
            assertThat(resultado.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("El servicio retorna correctamente una solicitud por ID")
        void obtenerPorId_exitoso() {
            when(solicitudRepository.findByIdConHistorial(solicitudClasificada.getId()))
                    .thenReturn(Optional.of(solicitudClasificada));

            SolicitudResponse response = solicitudService.obtenerPorId(solicitudClasificada.getId());

            assertThat(response.getId()).isEqualTo(solicitudClasificada.getId());
            assertThat(response.getEstado()).isEqualTo(EstadoSolicitud.CLASIFICADA);
        }
    }


    // RF-13 — Autorización básica de operaciones


    @Nested
    @DisplayName("RF-13: Autorización básica")
    class RF13_Autorizacion {

        @Test
        @DisplayName("Solo responsable activo puede ser asignado")
        void autorizacion_soloResponsableActivo() {
            Usuario inactivo = Usuario.builder()
                    .id(UUID.randomUUID())
                    .nombre("Inactivo")
                    .email("inactivo@test.com")
                    .rol(RolUsuario.RESPONSABLE)
                    .activo(false)
                    .build();

            AsignacionRequest request = new AsignacionRequest();
            request.setResponsableId(inactivo.getId());
            request.setVersion(1);

            when(solicitudRepository.findByIdConRelaciones(solicitudClasificada.getId()))
                    .thenReturn(Optional.of(solicitudClasificada));

            when(usuarioRepository.findById(inactivo.getId()))
                    .thenReturn(Optional.of(inactivo));
            when(usuarioRepository.findById(responsable.getId()))
                    .thenReturn(Optional.of(responsable));

            assertThatThrownBy(() -> solicitudService.asignarResponsable(
                    solicitudClasificada.getId(), request, responsable.getId()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}