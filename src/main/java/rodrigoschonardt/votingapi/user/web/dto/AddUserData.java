package rodrigoschonardt.votingapi.user.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddUserData(
        @NotNull @Schema(example = "123.456.789-00") @Size(min = 14, max = 14, message = "CPF deve ter exatamente 14 caracteres") String cpf) {
}
