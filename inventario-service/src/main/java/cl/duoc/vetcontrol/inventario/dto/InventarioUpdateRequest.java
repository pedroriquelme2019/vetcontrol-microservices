package cl.duoc.vetcontrol.inventario.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InventarioUpdateRequest(

        @NotNull
        @PositiveOrZero
        Integer stockActual,

        @NotNull
        @PositiveOrZero
        Integer stockMinimo

) {
}