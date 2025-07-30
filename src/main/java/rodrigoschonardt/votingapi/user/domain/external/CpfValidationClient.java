package rodrigoschonardt.votingapi.user.domain.external;

import rodrigoschonardt.votingapi.user.domain.external.dto.CpfValidationResponse;

public interface CpfValidationClient {
    CpfValidationResponse validate(String cpf);
}
