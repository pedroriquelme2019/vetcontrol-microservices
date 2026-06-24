package cl.duoc.vetcontrol.historial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class HistorialServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                HistorialServiceApplication.class,
                args
        );
    }
}