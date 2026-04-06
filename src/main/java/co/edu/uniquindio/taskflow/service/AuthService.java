package co.edu.uniquindio.taskflow.service;

import co.edu.uniquindio.taskflow.dto.request.LoginRequest;
import co.edu.uniquindio.taskflow.dto.request.RegistroUsuarioRequest;
import co.edu.uniquindio.taskflow.dto.response.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest request);
    TokenResponse registrar(RegistroUsuarioRequest request);
}
