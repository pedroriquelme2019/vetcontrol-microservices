package cl.duoc.vetcontrol.historial.messaging;

import cl.duoc.vetcontrol.historial.service.HistorialService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class HistorialEventListener {

    private static final Logger log = LoggerFactory.getLogger(HistorialEventListener.class);

    private final HistorialService service;
    private final ObjectMapper objectMapper;

    public HistorialEventListener(HistorialService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    // CORREGIDO: ahora parsea el payload JSON y persiste el historial correctamente
    @KafkaListener(topics = "atencion-registrada", groupId = "historial-service")
    public void onAtencion(String payload) {
        log.info("Evento recibido atencion-registrada: {}", payload);
        try {
            JsonNode node = objectMapper.readTree(payload);
            Long atencionId = node.get("atencionId").asLong();
            Long mascotaId  = node.get("mascotaId").asLong();
            service.registrarDesdeAtencion(mascotaId, atencionId);
            log.info("Historial registrado para mascotaId={} atencionId={}", mascotaId, atencionId);
        } catch (Exception ex) {
            log.error("Error al procesar evento atencion-registrada. Payload: {}", payload, ex);
        }
    }
}
