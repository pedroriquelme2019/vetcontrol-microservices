package cl.duoc.vetcontrol.historial.service;

import cl.duoc.vetcontrol.historial.client.MascotaClient;
import cl.duoc.vetcontrol.historial.dto.HistorialRequest;
import cl.duoc.vetcontrol.historial.exception.BusinessException;
import cl.duoc.vetcontrol.historial.exception.ResourceNotFoundException;
import cl.duoc.vetcontrol.historial.model.HistorialClinico;
import cl.duoc.vetcontrol.historial.repository.HistorialRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class HistorialService {

    private static final Logger log =
            LoggerFactory.getLogger(HistorialService.class);

    private static final String TIPO_ATENCION =
            "ATENCION";

    private static final Set<String> TIPOS_PERMITIDOS =
            Set.of(
                    "ATENCION",
                    "VACUNA",
                    "CIRUGIA",
                    "EXAMEN",
                    "ALERGIA",
                    "MEDICAMENTO",
                    "OBSERVACION"
            );

    private final HistorialRepository repository;
    private final MascotaClient mascotaClient;

    public HistorialService(
            HistorialRepository repository,
            MascotaClient mascotaClient
    ) {
        this.repository = repository;
        this.mascotaClient = mascotaClient;
    }

    @Transactional(readOnly = true)
    public List<HistorialClinico> findAll() {
        return repository
                .findAllByOrderByFechaDesc();
    }

    @Transactional(readOnly = true)
    public HistorialClinico findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Historial no encontrado: " + id
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<HistorialClinico> findByMascota(
            Long mascotaId
    ) {
        return repository
                .findByMascotaIdOrderByFechaDesc(
                        mascotaId
                );
    }

    @Transactional
    public HistorialClinico create(
            HistorialRequest request
    ) {
        validarMascota(
                request.mascotaId()
        );

        validarFecha(
                request.fecha()
        );

        String tipo =
                normalizarTipo(
                        request.tipo()
                );

        validarReferenciaNoDuplicada(
                tipo,
                request.referenciaExternaId()
        );

        HistorialClinico historial =
                new HistorialClinico();

        historial.setMascotaId(
                request.mascotaId()
        );

        historial.setFecha(
                request.fecha()
        );

        historial.setTipo(
                tipo
        );

        historial.setDetalle(
                request.detalle().trim()
        );

        historial.setReferenciaExternaId(
                request.referenciaExternaId()
        );

        HistorialClinico guardado =
                repository.save(historial);

        log.info(
                "Historial creado id={} mascota={} tipo={}",
                guardado.getId(),
                guardado.getMascotaId(),
                guardado.getTipo()
        );

        return guardado;
    }

    @Transactional
    public HistorialClinico registrarDesdeAtencion(
            Long mascotaId,
            Long atencionId
    ) {
        validarIdentificador(
                mascotaId,
                "mascotaId"
        );

        validarIdentificador(
                atencionId,
                "atencionId"
        );

        Optional<HistorialClinico> existente =
                repository
                        .findByTipoAndReferenciaExternaId(
                                TIPO_ATENCION,
                                atencionId
                        );

        if (existente.isPresent()) {

            log.info(
                    "Evento de atención duplicado ignorado. atencionId={}",
                    atencionId
            );

            return existente.get();
        }

        HistorialClinico historial =
                new HistorialClinico();

        historial.setMascotaId(
                mascotaId
        );

        historial.setFecha(
                LocalDateTime.now()
        );

        historial.setTipo(
                TIPO_ATENCION
        );

        historial.setDetalle(
                "Atención veterinaria registrada automáticamente desde Kafka"
        );

        historial.setReferenciaExternaId(
                atencionId
        );

        HistorialClinico guardado =
                repository.save(historial);

        log.info(
                "Historial automático registrado id={} mascota={} atención={}",
                guardado.getId(),
                mascotaId,
                atencionId
        );

        return guardado;
    }

    private void validarMascota(Long mascotaId) {
        try {
            Map<String, Object> mascota =
                    mascotaClient.findById(
                            mascotaId
                    );

            if (mascota == null
                    || mascota.isEmpty()) {

                throw new BusinessException(
                        "La mascota no existe o no está disponible"
                );
            }

            Object activo =
                    mascota.get("activo");

            if (activo instanceof Boolean
                    && !((Boolean) activo)) {

                throw new BusinessException(
                        "La mascota no existe o no está disponible"
                );
            }

        } catch (FeignException exception) {
            throw new BusinessException(
                    "La mascota no existe o no está disponible"
            );
        }
    }

    private void validarFecha(
            LocalDateTime fecha
    ) {
        if (fecha.isAfter(LocalDateTime.now())) {
            throw new BusinessException(
                    "La fecha del historial no puede estar en el futuro"
            );
        }
    }

    private String normalizarTipo(
            String tipo
    ) {
        String normalizado =
                tipo.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (!TIPOS_PERMITIDOS.contains(
                normalizado
        )) {
            throw new BusinessException(
                    "Tipo de historial no permitido"
            );
        }

        return normalizado;
    }

    private void validarReferenciaNoDuplicada(
            String tipo,
            Long referenciaExternaId
    ) {
        if (referenciaExternaId == null) {
            return;
        }

        boolean existe =
                repository
                        .findByTipoAndReferenciaExternaId(
                                tipo,
                                referenciaExternaId
                        )
                        .isPresent();

        if (existe) {
            throw new BusinessException(
                    "Ya existe un historial del tipo "
                            + tipo
                            + " con referencia externa "
                            + referenciaExternaId
            );
        }
    }

    private void validarIdentificador(
            Long id,
            String nombre
    ) {
        if (id == null || id <= 0) {
            throw new BusinessException(
                    nombre + " debe ser mayor que cero"
            );
        }
    }
}