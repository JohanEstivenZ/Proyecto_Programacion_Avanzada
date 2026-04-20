package co.edu.uniquindio.taskflow.dto.request;

import co.edu.uniquindio.taskflow.domain.enums.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotNull
    private RolUsuario rol;

    private Boolean activo = true;
}