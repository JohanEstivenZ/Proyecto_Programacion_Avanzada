package co.edu.uniquindio.taskflow.service.impl;

import co.edu.uniquindio.taskflow.config.JwtUtils;
import co.edu.uniquindio.taskflow.domain.model.Usuario;
import co.edu.uniquindio.taskflow.dto.request.LoginRequest;
import co.edu.uniquindio.taskflow.dto.request.RegistroUsuarioRequest;
import co.edu.uniquindio.taskflow.dto.response.TokenResponse;
import co.edu.uniquindio.taskflow.exception.ReglaNegocioException;
import co.edu.uniquindio.taskflow.repository.UsuarioRepository;
import co.edu.uniquindio.taskflow.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementacion del servicio de autenticacion.
 *
 * <p>{@code @Service} registra esta clase como bean de servicio en Spring.
 * Es semanticamente equivalente a {@code @Component}, pero indica
 * que la clase pertenece a la capa de logica de negocio.</p>
 *
 * <p>{@code @Transactional} envuelve cada metodo en una transaccion de BD.
 * Si ocurre una {@code RuntimeException}, la transaccion hace rollback
 * automaticamente (los datos no se persisten). Si el metodo termina
 * exitosamente, se hace commit.</p>
 *
 * <p>{@code @RequiredArgsConstructor} (Lombok) genera un constructor con
 * los campos {@code final}. Spring usa este constructor para inyectar
 * las dependencias. Esto se llama <strong>inyeccion por constructor</strong>,
 * que es la forma recomendada (vs. {@code @Autowired} en el campo).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("Intento de login: {}", request.getEmail());

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        if (!usuario.getActivo()) {
            throw new ReglaNegocioException("Cuenta desactivada");
        }

        return buildTokenResponse(usuario);
    }

    @Override
    @Transactional
    public TokenResponse registrar(RegistroUsuarioRequest request) {
        log.info("Registrando usuario: {}", request.getEmail());

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ReglaNegocioException("Ya existe un usuario con ese email");
        }
        if (usuarioRepository.existsByIdentificacion(request.getIdentificacion())) {
            throw new ReglaNegocioException("Ya existe un usuario con esa identificacion");
        }

        Usuario usuario = Usuario.builder()
                .identificacion(request.getIdentificacion())
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .activo(true)
                .build();

        usuario = usuarioRepository.save(usuario);
        log.info("Usuario registrado: {}", usuario.getId());

        return buildTokenResponse(usuario);
    }

    private TokenResponse buildTokenResponse(Usuario usuario) {
        return TokenResponse.builder()
                .token(jwtUtils.generarToken(usuario))
                .tipo("Bearer")
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .rol(usuario.getRol().name())
                .build();
    }
}
