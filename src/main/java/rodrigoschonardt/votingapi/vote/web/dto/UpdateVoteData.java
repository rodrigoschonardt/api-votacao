package rodrigoschonardt.votingapi.vote.web.dto;

import jakarta.validation.constraints.NotNull;
import rodrigoschonardt.votingapi.vote.domain.model.Vote;

public record UpdateVoteData(@NotNull Long id, @NotNull Vote.VoteOption voteOption) {
}
