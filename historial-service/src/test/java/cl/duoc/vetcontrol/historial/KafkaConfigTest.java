package cl.duoc.vetcontrol.historial;

import cl.duoc.vetcontrol.historial.config.KafkaConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConfigTest {

    @Test
    void debeTenerAnotacionesKafka() {
        assertTrue(
                KafkaConfig.class.isAnnotationPresent(
                        Configuration.class
                )
        );

        assertTrue(
                KafkaConfig.class.isAnnotationPresent(
                        EnableKafka.class
                )
        );
    }

    @Test
    void debeCrearTopicAtencionRegistrada() {
        NewTopic topic =
                new KafkaConfig()
                        .atencionRegistradaTopic();

        assertAll(
                () -> assertNotNull(topic),
                () -> assertEquals(
                        "atencion-registrada",
                        topic.name()
                ),
                () -> assertEquals(
                        1,
                        topic.numPartitions()
                ),
                () -> assertEquals(
                        (short) 1,
                        topic.replicationFactor()
                )
        );
    }

    @Test
    void constanteDebeSerCorrecta() {
        assertEquals(
                "atencion-registrada",
                KafkaConfig.TOPIC_ATENCION_REGISTRADA
        );
    }
}