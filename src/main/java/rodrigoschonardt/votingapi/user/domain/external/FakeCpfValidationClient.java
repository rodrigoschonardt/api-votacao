package rodrigoschonardt.votingapi.user.domain.external;

import org.springframework.stereotype.Component;
import rodrigoschonardt.votingapi.user.domain.external.dto.CpfValidationResponse;

import java.util.Random;

@Component
public class FakeCpfValidationClient implements CpfValidationClient {

    private final Random random = new Random();

    @Override
    public CpfValidationResponse validate(String cpf) {
        if (cpf.isBlank()) {
            return new CpfValidationResponse(CpfValidationResponse.UNABLE);
        }

        boolean canVote = random.nextBoolean();
        String status = canVote ? CpfValidationResponse.ABLE : CpfValidationResponse.UNABLE;

        return new CpfValidationResponse(status);
    }
}
