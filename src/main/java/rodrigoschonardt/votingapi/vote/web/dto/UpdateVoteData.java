package rodrigoschonardt.votingapi.vote.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import rodrigoschonardt.votingapi.vote.domain.model.Vote;

public record UpdateVoteData(@NotNull Long id,
                             @NotNull @Schema(example = "sim", allowableValues = {"sim", "não"}) Vote.VoteOption voteOption) {
}
