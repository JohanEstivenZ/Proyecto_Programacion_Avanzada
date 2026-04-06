package co.edu.uniquindio.taskflow.service;

import co.edu.uniquindio.taskflow.domain.enums.*;
import co.edu.uniquindio.taskflow.dto.request.*;
import co.edu.uniquindio.taskflow.dto.response.TareaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface TareaService {
    TareaResponse crear(TareaCrearRequest request, UUID creadorId);
    TareaResponse clasificar(UUID tareaId, ClasificarTareaRequest request, UUID actorId);
    TareaResponse asignar(UUID tareaId, AsignarTareaRequest request, UUID actorId);
    TareaResponse iniciar(UUID tareaId, VersionRequest request, UUID actorId);
    TareaResponse enviarARevision(UUID tareaId, VersionRequest request, UUID actorId);
    TareaResponse completar(UUID tareaId, VersionRequest request, UUID actorId);
    TareaResponse archivar(UUID tareaId, ArchivarTareaRequest request, UUID actorId);
    TareaResponse obtenerPorId(UUID tareaId);
    Page<TareaResponse> listar(EstadoTarea estado, Categoria categoria, Prioridad prioridad, UUID asignadoId, Pageable pageable);
}
