package cl.duoc.vetcontrol.notificacion;

import cl.duoc.vetcontrol.notificacion.config.KafkaConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import static org.junit.jupiter.api.Assertions.*;

class KafkaConfigTest {

    @Test
    void debeTenerAnotacionesKafka() {
        assertTrue(
                KafkaConfig.class
                        .isAnnotationPresent(
                                Configuration.class
                        )
        );

        assertTrue(
                KafkaConfig.class
                        .isAnnotationPresent(
                                EnableKafka.class
                        )
        );
    }

    @Test
    void debeCrearTopicCitaCreada() {
        NewTopic topic =
                new KafkaConfig()
                        .citaCreadaTopic();

        verificarTopic(
                topic,
                "cita-creada"
        );
    }

    @Test
    void debeCrearTopicVentaCreada() {
        NewTopic topic =
                new KafkaConfig()
                        .ventaCreadaTopic();

        verificarTopic(
                topic,
                "venta-creada"
        );
    }

    @Test
    void debeCrearTopicAtencionRegistrada() {
        NewTopic topic =
                new KafkaConfig()
                        .atencionRegistradaTopic();

        verificarTopic(
                topic,
                "atencion-registrada"
        );
    }

    @Test
    void constantesDebenSerCorrectas() {
        assertAll(
                () -> assertEquals(
                        "cita-creada",
                        KafkaConfig.TOPIC_CITA_CREADA
                ),
                () -> assertEquals(
                        "venta-creada",
                        KafkaConfig.TOPIC_VENTA_CREADA
                ),
                () -> assertEquals(
                        "atencion-registrada",
                        KafkaConfig.TOPIC_ATENCION_REGISTRADA
                )
        );
    }

    private void verificarTopic(
            NewTopic topic,
            String nombre
    ) {
        assertAll(
                () -> assertNotNull(topic),
                () -> assertEquals(
                        nombre,
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
}