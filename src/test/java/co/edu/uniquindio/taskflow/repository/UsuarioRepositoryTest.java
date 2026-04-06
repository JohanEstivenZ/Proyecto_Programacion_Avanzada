package co.edu.uniquindio.taskflow.repository;

import co.edu.uniquindio.taskflow.domain.enums.Rol;
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
        em.persistAndFlush(Usuario.builder().identificacion("1001").nombre("Admin")
                .email("admin@test.com").password("hash").rol(Rol.ADMIN).activo(true).build());
        em.persistAndFlush(Usuario.builder().identificacion("2001").nombre("Lead")
                .email("lead@test.com").password("hash").rol(Rol.TEAM_LEAD).activo(true).build());
        em.persistAndFlush(Usuario.builder().identificacion("3001").nombre("Dev Inactivo")
                .email("inactivo@test.com").password("hash").rol(Rol.DEVELOPER).activo(false).build());
    }

    @Test @DisplayName("findByEmail existente")
    void findByEmail() {
        Optional<Usuario> res = repo.findByEmail("admin@test.com");
        assertThat(res).isPresent();
        assertThat(res.get().getRol()).isEqualTo(Rol.ADMIN);
    }

    @Test @DisplayName("findByEmail inexistente")
    void findByEmail_noExiste() {
        assertThat(repo.findByEmail("nope@test.com")).isEmpty();
    }

    @Test @DisplayName("existsByEmail true/false")
    void existsByEmail() {
        assertThat(repo.existsByEmail("lead@test.com")).isTrue();
        assertThat(repo.existsByEmail("ghost@test.com")).isFalse();
    }

    @Test @DisplayName("findByActivoTrue excluye inactivos")
    void findByActivoTrue() {
        List<Usuario> activos = repo.findByActivoTrue();
        assertThat(activos).hasSize(2);
        assertThat(activos).extracting(Usuario::getNombre).doesNotContain("Dev Inactivo");
    }

    @Test @DisplayName("findByRolAndActivoTrue filtra rol + activo")
    void findByRolAndActivoTrue() {
        assertThat(repo.findByRolAndActivoTrue(Rol.DEVELOPER)).isEmpty();
        assertThat(repo.findByRolAndActivoTrue(Rol.TEAM_LEAD)).hasSize(1);
    }
}
