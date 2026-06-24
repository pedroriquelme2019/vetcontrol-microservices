package cl.duoc.vetcontrol.venta.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DetalleVentaRequest(

        @NotNull
        @Positive
        Long productoId,

        @NotNull
        @Positive
        Integer cantidad

) {
}