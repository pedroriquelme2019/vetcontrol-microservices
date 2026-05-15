package cl.duoc.vetcontrol.historial.messaging;
import cl.duoc.vetcontrol.historial.service.HistorialService;
import org.slf4j.*; import org.springframework.kafka.annotation.KafkaListener; import org.springframework.stereotype.Component;
@Component
public class HistorialEventListener { private static final Logger log=LoggerFactory.getLogger(HistorialEventListener.class); private final HistorialService service; public HistorialEventListener(HistorialService service){this.service=service;} @KafkaListener(topics="atencion-registrada", groupId="historial-service") public void onAtencion(String payload){ log.info("Evento recibido atencion-registrada: {}", payload); /* En una entrega final se parsea el JSON y se persiste el historial. */ } }
