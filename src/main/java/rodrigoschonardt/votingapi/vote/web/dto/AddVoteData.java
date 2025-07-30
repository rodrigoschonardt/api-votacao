package rodrigoschonardt.votingapi.vote.web.dto;

import jakarta.validation.constraints.NotNull;
import rodrigoschonardt.votingapi.vote.domain.model.Vote;

public record AddVoteData(@NotNull Vote.VoteOption voteOption, @NotNull Long userId, @NotNull Long sessionId) {
}
