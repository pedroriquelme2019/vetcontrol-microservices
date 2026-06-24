package cl.duoc.vetcontrol.notificacion.messaging;

import cl.duoc.vetcontrol.notificacion.config.KafkaConfig;
import cl.duoc.vetcontrol.notificacion.model.TipoNotificacion;
import cl.duoc.vetcontrol.notificacion.service.NotificacionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    NotificationListener.class
            );

    private final NotificacionService service;
    private final ObjectMapper objectMapper;

    public NotificationListener(
            NotificacionService service,
            ObjectMapper objectMapper
    ) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_CITA_CREADA,
            groupId = "${spring.kafka.consumer.group-id:notificacion-service}"
    )
    public void cita(String payload) {
        procesarEvento(
                payload,
                KafkaConfig.TOPIC_CITA_CREADA,
                "CITA_CREADA",
                "citaId",
                TipoNotificacion.CITA
        );
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_VENTA_CREADA,
            groupId = "${spring.kafka.consumer.group-id:notificacion-service}"
    )
    public void venta(String payload) {
        procesarEvento(
                payload,
                KafkaConfig.TOPIC_VENTA_CREADA,
                "VENTA_CREADA",
                "ventaId",
                TipoNotificacion.VENTA
        );
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_ATENCION_REGISTRADA,
            groupId = "${spring.kafka.consumer.group-id:notificacion-service}"
    )
    public void atencion(String payload) {
        procesarEvento(
                payload,
                KafkaConfig.TOPIC_ATENCION_REGISTRADA,
                "ATENCION_REGISTRADA",
                "atencionId",
                TipoNotificacion.ATENCION
        );
    }

    private void procesarEvento(
            String payload,
            String topic,
            String tipoEsperado,
            String campoId,
            TipoNotificacion tipoNotificacion
    ) {
        try {
            JsonNode node =
                    objectMapper.readTree(payload);

            validarObjeto(node);

            String tipoEvento =
                    node.path("tipo")
                            .asText("");

            if (!tipoEvento.isBlank()
                    && !tipoEsperado.equals(tipoEvento)) {

                throw new IllegalArgumentException(
                        "El tipo del evento no corresponde al topic"
                );
            }

            Long referenciaId =
                    obtenerId(
                            node,
                            campoId
                    );

            String mensaje =
                    construirMensaje(
                            tipoNotificacion,
                            referenciaId,
                            node
                    );

            service.registrarDesdeEvento(
                    tipoNotificacion,
                    mensaje,
                    topic,
                    referenciaId
            );

            log.info(
                    "Evento procesado topic={} referencia={}",
                    topic,
                    referenciaId
            );

        } catch (Exception exception) {
            log.error(
                    "Evento inválido descartado. topic={} payload={}",
                    topic,
                    payload,
                    exception
            );
        }
    }

    private void validarObjeto(
            JsonNode node
    ) {
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException(
                    "El evento debe ser un objeto JSON"
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
                    "El campo "
                            + campo
                            + " no es válido"
            );
        }

        long id =
                valor.asLong();

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El campo "
                            + campo
                            + " debe ser mayor que cero"
            );
        }

        return id;
    }

    private String construirMensaje(
            TipoNotificacion tipo,
            Long referenciaId,
            JsonNode node
    ) {
        return switch (tipo) {

            case CITA ->
                    construirMensajeCita(
                            referenciaId,
                            node
                    );

            case VENTA ->
                    construirMensajeVenta(
                            referenciaId,
                            node
                    );

            case ATENCION ->
                    construirMensajeAtencion(
                            referenciaId,
                            node
                    );

            default ->
                    "Nueva notificación registrada";
        };
    }

    private String construirMensajeCita(
            Long citaId,
            JsonNode node
    ) {
        StringBuilder mensaje =
                new StringBuilder(
                        "Nueva cita registrada. Cita #"
                                + citaId
                );

        agregarDato(
                mensaje,
                node,
                "mascotaId",
                ", mascota #"
        );

        agregarDato(
                mensaje,
                node,
                "fecha",
                ", fecha "
        );

        agregarDato(
                mensaje,
                node,
                "hora",
                " "
        );

        return mensaje.toString();
    }

    private String construirMensajeVenta(
            Long ventaId,
            JsonNode node
    ) {
        StringBuilder mensaje =
                new StringBuilder(
                        "Nueva venta registrada. Venta #"
                                + ventaId
                );

        agregarDato(
                mensaje,
                node,
                "clienteId",
                ", cliente #"
        );

        agregarDato(
                mensaje,
                node,
                "total",
                ", total $"
        );

        return mensaje.toString();
    }

    private String construirMensajeAtencion(
            Long atencionId,
            JsonNode node
    ) {
        StringBuilder mensaje =
                new StringBuilder(
                        "Nueva atención registrada. Atención #"
                                + atencionId
                );

        agregarDato(
                mensaje,
                node,
                "mascotaId",
                ", mascota #"
        );

        return mensaje.toString();
    }

    private void agregarDato(
            StringBuilder mensaje,
            JsonNode node,
            String campo,
            String prefijo
    ) {
        JsonNode valor =
                node.path(campo);

        if (!valor.isMissingNode()
                && !valor.isNull()
                && !valor.asText().isBlank()) {

            mensaje.append(prefijo)
                    .append(valor.asText());
        }
    }
}