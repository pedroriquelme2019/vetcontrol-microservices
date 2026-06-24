package cl.duoc.vetcontrol.historial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record HistorialRequest(

        @NotNull
        @Positive
        Long mascotaId,

        @NotNull
        @PastOrPresent
        LocalDateTime fecha,

        @NotBlank
        @Size(max = 60)
        @Pattern(
                regexp = "(?i)ATENCION|VACUNA|CIRUGIA|EXAMEN|ALERGIA|MEDICAMENTO|OBSERVACION",
                message = "debe ser ATENCION, VACUNA, CIRUGIA, EXAMEN, ALERGIA, MEDICAMENTO u OBSERVACION"
        )
        String tipo,

        @NotBlank
        @Size(max = 500)
        String detalle,

        @Positive
        Long referenciaExternaId

) {
}