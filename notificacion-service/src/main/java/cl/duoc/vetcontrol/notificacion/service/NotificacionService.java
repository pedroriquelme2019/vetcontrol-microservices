package cl.duoc.vetcontrol.notificacion.service;

import cl.duoc.vetcontrol.notificacion.dto.NotificacionRequest;
import cl.duoc.vetcontrol.notificacion.exception.BusinessException;
import cl.duoc.vetcontrol.notificacion.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.notificacion.model.Notificacion;
import cl.duoc.vetcontrol.notificacion.model.TipoNotificacion;
import cl.duoc.vetcontrol.notificacion.repository.NotificacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class NotificacionService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    NotificacionService.class
            );

    private final NotificacionRepository repository;

    public NotificacionService(
            NotificacionRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Notificacion> findAll() {
        return repository.findAllByOrderByFechaDesc();
    }

    @Transactional(readOnly = true)
    public Notificacion findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Notificación no encontrada: " + id
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Notificacion> findNoLeidas() {
        return repository
                .findByLeidaFalseOrderByFechaDesc();
    }

    @Transactional(readOnly = true)
    public List<Notificacion> findByTipo(
            String tipo
    ) {
        TipoNotificacion tipoNormalizado =
                convertirTipo(tipo);

        return repository
                .findByTipoOrderByFechaDesc(
                        tipoNormalizado
                );
    }

    @Transactional
    public Notificacion create(
            NotificacionRequest request
    ) {
        Notificacion notificacion =
                new Notificacion();

        notificacion.setTipo(
                request.tipo()
        );

        notificacion.setMensaje(
                validarMensaje(request.mensaje())
        );

        notificacion.setLeida(false);
        notificacion.setOrigenEvento("API");

        Notificacion guardada =
                repository.save(notificacion);

        log.info(
                "Notificación manual creada id={} tipo={}",
                guardada.getId(),
                guardada.getTipo()
        );

        return guardada;
    }

    @Transactional
    public Notificacion registrarDesdeEvento(
            TipoNotificacion tipo,
            String mensaje,
            String origenEvento,
            Long referenciaExternaId
    ) {
        validarEvento(
                tipo,
                origenEvento,
                referenciaExternaId
        );

        String origenLimpio =
                origenEvento.trim();

        String claveEvento =
                origenLimpio
                        + ":"
                        + referenciaExternaId;

        Optional<Notificacion> existente =
                repository.findByClaveEvento(
                        claveEvento
                );

        if (existente.isPresent()) {
            log.info(
                    "Evento duplicado ignorado. clave={}",
                    claveEvento
            );

            return existente.get();
        }

        Notificacion notificacion =
                new Notificacion();

        notificacion.setTipo(tipo);

        notificacion.setMensaje(
                validarMensaje(mensaje)
        );

        notificacion.setOrigenEvento(
                origenLimpio
        );

        notificacion.setReferenciaExternaId(
                referenciaExternaId
        );

        notificacion.setClaveEvento(
                claveEvento
        );

        notificacion.setLeida(false);

        try {
            Notificacion guardada =
                    repository.saveAndFlush(
                            notificacion
                    );

            log.info(
                    "Notificación de evento creada id={} clave={}",
                    guardada.getId(),
                    claveEvento
            );

            return guardada;

        } catch (DataIntegrityViolationException exception) {

            log.warn(
                    "Evento registrado por otra ejecución. clave={}",
                    claveEvento
            );

            return repository
                    .findByClaveEvento(claveEvento)
                    .orElseThrow(() -> exception);
        }
    }

    @Transactional
    public Notificacion marcarComoLeida(
            Long id
    ) {
        Notificacion notificacion =
                findById(id);

        if (!notificacion.isLeida()) {
            notificacion.setLeida(true);
            repository.save(notificacion);
        }

        return notificacion;
    }

    @Transactional
    public int marcarTodasComoLeidas() {
        List<Notificacion> pendientes =
                repository
                        .findByLeidaFalseOrderByFechaDesc();

        pendientes.forEach(
                notificacion ->
                        notificacion.setLeida(true)
        );

        if (!pendientes.isEmpty()) {
            repository.saveAll(pendientes);
        }

        return pendientes.size();
    }

    @Transactional
    public void delete(Long id) {
        Notificacion notificacion =
                findById(id);

        repository.delete(notificacion);

        log.info(
                "Notificación eliminada id={}",
                id
        );
    }

    private TipoNotificacion convertirTipo(
            String tipo
    ) {
        if (tipo == null || tipo.isBlank()) {
            throw new BusinessException(
                    "El tipo de notificación es obligatorio"
            );
        }

        try {
            return TipoNotificacion.valueOf(
                    tipo.trim()
                            .toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException exception) {
            throw new BusinessException(
                    "Tipo de notificación no permitido"
            );
        }
    }

    private String validarMensaje(
            String mensaje
    ) {
        if (mensaje == null || mensaje.isBlank()) {
            throw new BusinessException(
                    "El mensaje es obligatorio"
            );
        }

        String mensajeLimpio =
                mensaje.trim();

        if (mensajeLimpio.length() > 500) {
            throw new BusinessException(
                    "El mensaje no puede superar los 500 caracteres"
            );
        }

        return mensajeLimpio;
    }

    private void validarEvento(
            TipoNotificacion tipo,
            String origenEvento,
            Long referenciaExternaId
    ) {
        if (tipo == null) {
            throw new BusinessException(
                    "El tipo del evento es obligatorio"
            );
        }

        if (origenEvento == null
                || origenEvento.isBlank()) {

            throw new BusinessException(
                    "El origen del evento es obligatorio"
            );
        }

        if (referenciaExternaId == null
                || referenciaExternaId <= 0) {

            throw new BusinessException(
                    "La referencia externa debe ser mayor que cero"
            );
        }
    }
}