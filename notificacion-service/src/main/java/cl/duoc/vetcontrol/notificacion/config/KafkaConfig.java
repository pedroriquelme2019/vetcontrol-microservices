package cl.duoc.vetcontrol.notificacion.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@EnableKafka
@Configuration
public class KafkaConfig {

    public static final String TOPIC_CITA_CREADA =
            "cita-creada";

    public static final String TOPIC_VENTA_CREADA =
            "venta-creada";

    public static final String TOPIC_ATENCION_REGISTRADA =
            "atencion-registrada";

    @Bean
    public NewTopic citaCreadaTopic() {
        return crearTopic(
                TOPIC_CITA_CREADA
        );
    }

    @Bean
    public NewTopic ventaCreadaTopic() {
        return crearTopic(
                TOPIC_VENTA_CREADA
        );
    }

    @Bean
    public NewTopic atencionRegistradaTopic() {
        return crearTopic(
                TOPIC_ATENCION_REGISTRADA
        );
    }

    private NewTopic crearTopic(
            String nombre
    ) {
        return TopicBuilder
                .name(nombre)
                .partitions(1)
                .replicas(1)
                .build();
    }
}