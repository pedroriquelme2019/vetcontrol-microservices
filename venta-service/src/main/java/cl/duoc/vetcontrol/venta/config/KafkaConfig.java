package cl.duoc.vetcontrol.venta.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@EnableKafka
@Configuration
public class KafkaConfig {

    public static final String TOPIC_VENTA_CREADA =
            "venta-creada";

    @Bean
    public NewTopic ventaCreadaTopic() {

        return TopicBuilder
                .name(TOPIC_VENTA_CREADA)
                .partitions(1)
                .replicas(1)
                .build();
    }
}