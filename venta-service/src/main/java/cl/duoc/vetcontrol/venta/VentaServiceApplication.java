package cl.duoc.vetcontrol.venta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableFeignClients
@SpringBootApplication
public class VentaServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VentaServiceApplication.class, args);
    }
}
