package co.edu.uniquindio.taskflow.repository;

import co.edu.uniquindio.taskflow.domain.enums.RolUsuario;
import co.edu.uniquindio.taskflow.domain.model.Usuario;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UsuarioRepository — Tests de persistencia")
class UsuarioRepositoryTest {

    @Autowired private TestEntityManager em;
    @Autowired private UsuarioRepository repo;

    @BeforeEach
    void setUp() {
        em.persistAndFlush(Usuario.builder()
                .nombre("Admin").email("admin@test.com")
                .password("hash").rol(RolUsuario.ADMIN).activo(true).build());
        em.persistAndFlush(Usuario.builder()
                .nombre("Responsable").email("responsable@test.com")
                .password("hash").rol(RolUsuario.RESPONSABLE).activo(true).build());
        em.persistAndFlush(Usuario.builder()
                .nombre("Estudiante Inactivo").email("inactivo@test.com")
                .password("hash").rol(RolUsuario.ESTUDIANTE).activo(false).build());
    }

    @Test
    @DisplayName("findByEmail existente retorna usuario")
    void findByEmail_existente() {
        Optional<Usuario> res = repo.findByEmail("admin@test.com");
        assertThat(res).isPresent();
        assertThat(res.get().getRol()).isEqualTo(RolUsuario.ADMIN);
    }

    @Test
    @DisplayName("findByEmail inexistente retorna vacío")
    void findByEmail_noExiste() {
        assertThat(repo.findByEmail("nope@test.com")).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail retorna true y false correctamente")
    void existsByEmail() {
        assertThat(repo.existsByEmail("responsable@test.com")).isTrue();
        assertThat(repo.existsByEmail("ghost@test.com")).isFalse();
    }

    @Test
    @DisplayName("findByActivoTrue excluye usuarios inactivos")
    void findByActivoTrue() {
        List<Usuario> activos = repo.findByActivoTrue();
        assertThat(activos).hasSize(2);
        assertThat(activos).extracting(Usuario::getNombre)
                .doesNotContain("Estudiante Inactivo");
    }

    @Test
    @DisplayName("findByRolAndActivoTrue filtra por rol y activo")
    void findByRolAndActivoTrue() {
        assertThat(repo.findByRolAndActivoTrue(RolUsuario.ESTUDIANTE)).isEmpty();
        assertThat(repo.findByRolAndActivoTrue(RolUsuario.RESPONSABLE)).hasSize(1);
    }
}