package cl.duoc.vetcontrol.notificacion.repository;

import cl.duoc.vetcontrol.notificacion.model.Notificacion;
import cl.duoc.vetcontrol.notificacion.model.TipoNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacionRepository
        extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findAllByOrderByFechaDesc();

    List<Notificacion> findByLeidaFalseOrderByFechaDesc();

    List<Notificacion> findByTipoOrderByFechaDesc(
            TipoNotificacion tipo
    );

    Optional<Notificacion> findByClaveEvento(
            String claveEvento
    );
}