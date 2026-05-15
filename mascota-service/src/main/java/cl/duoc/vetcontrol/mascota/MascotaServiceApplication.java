package cl.duoc.vetcontrol.mascota;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableFeignClients
@SpringBootApplication
public class MascotaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MascotaServiceApplication.class, args);
    }
}
