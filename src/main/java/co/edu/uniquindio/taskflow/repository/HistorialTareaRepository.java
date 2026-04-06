package co.edu.uniquindio.taskflow.repository;

import co.edu.uniquindio.taskflow.domain.model.HistorialTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialTareaRepository extends JpaRepository<HistorialTarea, UUID> {
    List<HistorialTarea> findByTareaIdOrderByFechaEventoAsc(UUID tareaId);
}
