package cl.duoc.vetcontrol.atencion;

import cl.duoc.vetcontrol.atencion.config.KafkaConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConfigTest {

    @Test
    void debeTenerAnotacionesDeConfiguracionKafka() {

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

        KafkaConfig config = new KafkaConfig();

        NewTopic topic =
                config.atencionRegistradaTopic();

        assertNotNull(topic);

        assertAll(
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
    void constanteDelTopicDebeSerCorrecta() {

        assertEquals(
                "atencion-registrada",
                KafkaConfig.TOPIC_ATENCION_REGISTRADA
        );
    }
}