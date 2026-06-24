package cl.duoc.vetcontrol.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record InventarioRequest(

        @NotNull
        @Positive
        Long productoId,

        @NotNull
        @PositiveOrZero
        Integer stockActual,

        @NotNull
        @PositiveOrZero
        Integer stockMinimo

) {
}