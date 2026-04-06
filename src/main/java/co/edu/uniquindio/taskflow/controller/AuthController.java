package co.edu.uniquindio.taskflow.controller;

import co.edu.uniquindio.taskflow.dto.request.LoginRequest;
import co.edu.uniquindio.taskflow.dto.request.RegistroUsuarioRequest;
import co.edu.uniquindio.taskflow.dto.response.TokenResponse;
import co.edu.uniquindio.taskflow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticacion y registro.
 *
 * <p>{@code @RestController} = {@code @Controller} + {@code @ResponseBody}.
 * Todos los metodos retornan objetos Java que Jackson serializa a JSON.</p>
 *
 * <p>{@code @RequestMapping("/auth")} define el prefijo de ruta.
 * URL completa: {@code http://localhost:8080/api/v1/auth/login}</p>
 *
 * <p>{@code @Valid} en un parametro {@code @RequestBody} activa la validacion
 * de Bean Validation. Si algun campo viola las restricciones ({@code @NotBlank},
 * {@code @Size}, etc.), Spring lanza {@code MethodArgumentNotValidException}
 * ANTES de ejecutar el metodo del controller.</p>
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacion", description = "Login y registro de usuarios")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion — retorna token JWT")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<TokenResponse> registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        return ResponseEntity.status(201).body(authService.registrar(request));
    }
}
