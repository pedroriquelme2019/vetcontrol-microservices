package cl.duoc.vetcontrol.historial.messaging;

import cl.duoc.vetcontrol.historial.config.KafkaConfig;
import cl.duoc.vetcontrol.historial.model.HistorialClinico;
import cl.duoc.vetcontrol.historial.service.HistorialService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class HistorialEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    HistorialEventListener.class
            );

    private static final String TIPO_EVENTO =
            "ATENCION_REGISTRADA";

    private final HistorialService service;
    private final ObjectMapper objectMapper;

    public HistorialEventListener(
            HistorialService service,
            ObjectMapper objectMapper
    ) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_ATENCION_REGISTRADA,
            groupId = "${spring.kafka.consumer.group-id:historial-service}"
    )
    public void onAtencion(String payload) {

        log.info(
                "Evento recibido desde {}: {}",
                KafkaConfig.TOPIC_ATENCION_REGISTRADA,
                payload
        );

        try {
            JsonNode node =
                    objectMapper.readTree(payload);

            String tipo =
                    node.path("tipo").asText("");

            if (!TIPO_EVENTO.equals(tipo)) {
                throw new IllegalArgumentException(
                        "El tipo del evento no corresponde a una atención"
                );
            }

            Long atencionId =
                    obtenerId(
                            node,
                            "atencionId"
                    );

            Long mascotaId =
                    obtenerId(
                            node,
                            "mascotaId"
                    );

            HistorialClinico historial =
                    service.registrarDesdeAtencion(
                            mascotaId,
                            atencionId
                    );

            log.info(
                    "Evento procesado. historialId={} mascotaId={} atencionId={}",
                    historial.getId(),
                    mascotaId,
                    atencionId
            );

        } catch (Exception exception) {
            log.error(
                    "Error al procesar evento {}. Payload: {}",
                    KafkaConfig.TOPIC_ATENCION_REGISTRADA,
                    payload,
                    exception
            );
        }
    }

    private Long obtenerId(
            JsonNode node,
            String campo
    ) {
        JsonNode valor =
                node.path(campo);

        if (!valor.canConvertToLong()) {
            throw new IllegalArgumentException(
                    "El campo " + campo + " no es válido"
            );
        }

        long id =
                valor.asLong();

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El campo " + campo
                            + " debe ser mayor que cero"
            );
        }

        return id;
    }
}