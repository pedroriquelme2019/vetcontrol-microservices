package cl.duoc.vetcontrol.venta;

import cl.duoc.vetcontrol.venta.config.KafkaConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConfigTest {

    @Test
    void debeTenerAnotacionesDeConfiguracion() {
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
    void debeCrearTopicVentaCreada() {
        NewTopic topic =
                new KafkaConfig().ventaCreadaTopic();

        assertAll(
                () -> assertEquals(
                        "venta-creada",
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
                "venta-creada",
                KafkaConfig.TOPIC_VENTA_CREADA
        );
    }
}