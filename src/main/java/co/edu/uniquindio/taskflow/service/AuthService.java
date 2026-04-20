package co.edu.uniquindio.taskflow.service;

import co.edu.uniquindio.taskflow.config.JwtUtil;
import co.edu.uniquindio.taskflow.domain.model.Usuario;
import co.edu.uniquindio.taskflow.dto.request.LoginRequest;
import co.edu.uniquindio.taskflow.dto.response.LoginResponse;
import co.edu.uniquindio.taskflow.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        if (!usuario.isActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }

        String token = jwtUtil.generarToken(usuario.getEmail(), usuario.getRol().name());
        return new LoginResponse(token);
    }
}