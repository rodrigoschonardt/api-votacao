package rodrigoschonardt.votingapi.session.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateSessionData(@NotNull Long id, @FutureOrPresent @Schema(description = "Data atual ou superior") LocalDateTime startTime,
                             @Schema(description = "Duração em minutos") @Min(1) Integer duration) {
}

