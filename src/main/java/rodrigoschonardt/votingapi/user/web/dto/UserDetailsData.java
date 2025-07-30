package rodrigoschonardt.votingapi.user.web.dto;

import java.time.LocalDateTime;

public record UserDetailsData(Long id, String cpf, LocalDateTime createdAt) {
}
