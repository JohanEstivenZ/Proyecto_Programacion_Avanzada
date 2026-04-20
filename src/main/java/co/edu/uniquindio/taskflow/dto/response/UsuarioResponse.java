package co.edu.uniquindio.taskflow.dto.response;

import co.edu.uniquindio.taskflow.domain.enums.RolUsuario;
import lombok.Data;
import java.util.UUID;

@Data
public class UsuarioResponse {
    private UUID id;
    private String nombre;
    private String email;
    private RolUsuario rol;
    private Boolean activo;
}