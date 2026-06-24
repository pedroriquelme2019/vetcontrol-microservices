package cl.duoc.vetcontrol.venta.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VentaRequest(

        @NotNull
        @Positive
        Long clienteId,

        @NotBlank
        @Size(max = 30)
        @Pattern(
                regexp = "(?i)EFECTIVO|DEBITO|CREDITO|TRANSFERENCIA",
                message = "debe ser EFECTIVO, DEBITO, CREDITO o TRANSFERENCIA"
        )
        String medioPago,

        @NotEmpty
        @Size(max = 50)
        List<@Valid DetalleVentaRequest> detalles

) {
}