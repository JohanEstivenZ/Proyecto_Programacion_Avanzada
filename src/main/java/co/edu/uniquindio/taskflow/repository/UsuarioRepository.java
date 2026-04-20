package co.edu.uniquindio.taskflow.repository;

import co.edu.uniquindio.taskflow.domain.enums.RolUsuario;
import co.edu.uniquindio.taskflow.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByActivoTrue();

    List<Usuario> findByRolAndActivoTrue(RolUsuario rol);
}