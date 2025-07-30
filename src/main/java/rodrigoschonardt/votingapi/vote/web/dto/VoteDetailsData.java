package rodrigoschonardt.votingapi.vote.web.dto;

import rodrigoschonardt.votingapi.session.web.dto.SessionDetailsData;
import rodrigoschonardt.votingapi.user.web.dto.UserDetailsData;
import rodrigoschonardt.votingapi.vote.domain.model.Vote;

import java.time.LocalDateTime;

public record VoteDetailsData(Long id, Vote.VoteOption voteOption, UserDetailsData user,
                              SessionDetailsData session, LocalDateTime createdAt) {
}
