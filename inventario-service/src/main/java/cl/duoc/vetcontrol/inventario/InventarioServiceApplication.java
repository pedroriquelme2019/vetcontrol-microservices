package cl.duoc.vetcontrol.inventario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class InventarioServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                InventarioServiceApplication.class,
                args
        );
    }
}