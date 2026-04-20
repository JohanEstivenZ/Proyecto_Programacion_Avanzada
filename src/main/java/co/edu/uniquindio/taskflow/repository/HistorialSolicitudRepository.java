package co.edu.uniquindio.taskflow.repository;

import co.edu.uniquindio.taskflow.domain.model.HistorialSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistorialSolicitudRepository extends JpaRepository<HistorialSolicitud, UUID> {

    List<HistorialSolicitud> findBySolicitudIdOrderByFechaHoraAsc(UUID solicitudId);
}