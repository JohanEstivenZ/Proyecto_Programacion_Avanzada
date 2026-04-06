package co.edu.uniquindio.taskflow.config;

import co.edu.uniquindio.taskflow.domain.enums.Rol;
import co.edu.uniquindio.taskflow.domain.model.Usuario;
import co.edu.uniquindio.taskflow.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Carga datos iniciales al arrancar la aplicacion.
 *
 * <p>{@code CommandLineRunner} es una interfaz funcional que Spring ejecuta
 * despues de que el contexto este completamente inicializado.
 * Se usa para seed data, migraciones, verificaciones iniciales, etc.</p>
 *
 * <p>{@code @Profile("!test")} indica que este bean NO se crea cuando el
 * perfil activo es "test". Asi los tests de integracion arrancan con
 * la BD vacia y no dependen de datos semilla.</p>
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            log.info("Inicializando datos de ejemplo...");

            usuarioRepository.save(Usuario.builder()
                    .identificacion("1001")
                    .nombre("Laura Rios - Admin")
                    .email("admin@taskflow.dev")
                    .password(passwordEncoder.encode("admin123"))
                    .rol(Rol.ADMIN).activo(true).build());

            usuarioRepository.save(Usuario.builder()
                    .identificacion("2001")
                    .nombre("Pedro Gomez - Team Lead")
                    .email("pedro@taskflow.dev")
                    .password(passwordEncoder.encode("lead123"))
                    .rol(Rol.TEAM_LEAD).activo(true).build());

            usuarioRepository.save(Usuario.builder()
                    .identificacion("3001")
                    .nombre("Ana Torres - Developer")
                    .email("ana@taskflow.dev")
                    .password(passwordEncoder.encode("dev123"))
                    .rol(Rol.DEVELOPER).activo(true).build());

            log.info("Usuarios creados: admin@taskflow.dev / pedro@taskflow.dev / ana@taskflow.dev");
        }
    }
}
