package co.edu.uniquindio.taskflow.repository;

import co.edu.uniquindio.taskflow.domain.enums.Rol;
import co.edu.uniquindio.taskflow.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la entidad {@link Usuario}.
 *
 * <p>{@code @Repository} marca esta interfaz como componente de persistencia.
 * Spring Data JPA genera la implementacion automaticamente en tiempo de ejecucion
 * basandose en la firma de los metodos (Query Derivation).</p>
 *
 * <p>{@code JpaRepository<Usuario, UUID>} hereda metodos CRUD:</p>
 * <ul>
 *   <li>{@code save(entity)} — INSERT o UPDATE (segun si tiene ID).</li>
 *   <li>{@code findById(id)} — SELECT por PK, retorna Optional.</li>
 *   <li>{@code findAll()} — SELECT * de la tabla.</li>
 *   <li>{@code deleteById(id)} — DELETE por PK.</li>
 *   <li>{@code count()} — SELECT COUNT(*).</li>
 *   <li>{@code existsById(id)} — SELECT EXISTS.</li>
 * </ul>
 *
 * <h3>Query Derivation</h3>
 * <p>Spring Data genera la query SQL a partir del nombre del metodo:</p>
 * <ul>
 *   <li>{@code findByEmail(email)} → {@code SELECT * FROM usuarios WHERE email = ?}</li>
 *   <li>{@code existsByEmail(email)} → {@code SELECT EXISTS(... WHERE email = ?)}</li>
 *   <li>{@code findByRolAndActivoTrue(rol)} → {@code SELECT * FROM usuarios WHERE rol = ? AND activo = true}</li>
 * </ul>
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByIdentificacion(String identificacion);

    List<Usuario> findByRolAndActivoTrue(Rol rol);

    List<Usuario> findByActivoTrue();
}
