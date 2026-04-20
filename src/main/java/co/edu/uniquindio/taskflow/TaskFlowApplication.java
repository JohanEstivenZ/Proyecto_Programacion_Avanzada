package co.edu.uniquindio.taskflow;

import co.edu.uniquindio.taskflow.domain.enums.RolUsuario;
import co.edu.uniquindio.taskflow.domain.model.Usuario;
import co.edu.uniquindio.taskflow.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Punto de entrada de la aplicacion TaskFlow.
 *
 * <p>{@code @SpringBootApplication} es una meta-anotacion que combina tres anotaciones
 * fundamentales de Spring:</p>
 *
 * <ul>
 *   <li>{@code @Configuration} — Indica que esta clase puede contener definiciones
 *       de beans mediante metodos anotados con {@code @Bean}.</li>
 *   <li>{@code @EnableAutoConfiguration} — Le dice a Spring Boot que configure
 *       automaticamente beans basandose en las dependencias del classpath.
 *       Por ejemplo, si {@code spring-boot-starter-web} esta en el classpath,
 *       Spring configura automaticamente un servidor Tomcat embebido.</li>
 *   <li>{@code @ComponentScan} — Escanea el paquete actual y todos sus
 *       subpaquetes en busca de clases anotadas con {@code @Component},
 *       {@code @Service}, {@code @Repository}, {@code @Controller}, etc.,
 *       y las registra como beans en el contenedor de Spring.</li>
 * </ul>
 *
 * <p>El metodo {@code main()} delega a {@link SpringApplication#run}, que:</p>
 * <ol>
 *   <li>Crea el ApplicationContext (contenedor IoC de Spring).</li>
 *   <li>Ejecuta la autoconfiguracion.</li>
 *   <li>Escanea componentes.</li>
 *   <li>Arranca el servidor embebido (Tomcat).</li>
 *   <li>Ejecuta los {@link org.springframework.boot.CommandLineRunner} registrados.</li>
 * </ol>
 *
 * @author Programa de Ingenieria de Sistemas - Universidad del Quindio
 * @version 1.0.0
 */
@SpringBootApplication
public class TaskFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskFlowApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedUsers(UsuarioRepository usuarioRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.count() == 0) {
                usuarioRepository.save(Usuario.builder()
                        .nombre("Admin")
                        .email("admin@taskflow.dev")
                        .password(passwordEncoder.encode("admin123"))
                        .rol(RolUsuario.ADMIN)
                        .activo(true)
                        .build());

                usuarioRepository.save(Usuario.builder()
                        .nombre("Responsable")
                        .email("pedro@taskflow.dev")
                        .password(passwordEncoder.encode("lead123"))
                        .rol(RolUsuario.RESPONSABLE)
                        .activo(true)
                        .build());

                usuarioRepository.save(Usuario.builder()
                        .nombre("Estudiante")
                        .email("ana@taskflow.dev")
                        .password(passwordEncoder.encode("dev123"))
                        .rol(RolUsuario.ESTUDIANTE)
                        .activo(true)
                        .build());
            }
        };
    }
}
