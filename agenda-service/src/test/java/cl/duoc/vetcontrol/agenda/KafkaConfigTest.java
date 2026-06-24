package cl.duoc.vetcontrol.agenda;

import cl.duoc.vetcontrol.agenda.config.KafkaConfig;
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
    void debeCrearTopicCitaCreada() {
        KafkaConfig config = new KafkaConfig();

        NewTopic topic = config.citaCreadaTopic();

        assertAll(
                () -> assertNotNull(topic),
                () -> assertEquals(
                        "cita-creada",
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
    void constanteDebeTenerNombreCorrecto() {
        assertEquals(
                "cita-creada",
                KafkaConfig.TOPIC_CITA_CREADA
        );
    }
}