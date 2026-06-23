package cl.duoc.vetcontrol.mascota;

import cl.duoc.vetcontrol.mascota.client.ClienteClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteClientTest {

    @Test
    void debeSerInterfazFeign() {

        assertTrue(
                ClienteClient.class.isInterface()
        );
    }
}