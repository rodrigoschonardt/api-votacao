package rodrigoschonardt.votingapi.user.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddUserData(
        @NotNull @Size(min = 14, max = 14, message = "CPF deve ter exatamente 14 caracteres") String cpf) {
}
