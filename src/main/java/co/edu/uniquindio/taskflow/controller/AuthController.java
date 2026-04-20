package co.edu.uniquindio.taskflow.controller;

import co.edu.uniquindio.taskflow.dto.request.LoginRequest;
import co.edu.uniquindio.taskflow.dto.response.LoginResponse;
import co.edu.uniquindio.taskflow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}