package rodrigoschonardt.votingapi.vote.web.mapper;

import org.springframework.stereotype.Component;
import rodrigoschonardt.votingapi.session.domain.model.Session;
import rodrigoschonardt.votingapi.session.web.mapper.SessionMapper;
import rodrigoschonardt.votingapi.user.domain.model.User;
import rodrigoschonardt.votingapi.user.web.mapper.UserMapper;
import rodrigoschonardt.votingapi.vote.domain.model.Vote;
import rodrigoschonardt.votingapi.vote.web.dto.AddVoteData;
import rodrigoschonardt.votingapi.vote.web.dto.UpdateVoteData;
import rodrigoschonardt.votingapi.vote.web.dto.VoteDetailsData;

import java.time.LocalDateTime;

@Component
public class VoteMapper {
    private final UserMapper userMapper;
    private final SessionMapper sessionMapper;

    public VoteMapper(UserMapper userMapper, SessionMapper sessionMapper) {
        this.userMapper = userMapper;
        this.sessionMapper = sessionMapper;
    }

    public Vote toEntity(AddVoteData voteData, User user, Session session) {
        Vote vote = new Vote();

        vote.setVoteOption(voteData.voteOption());
        vote.setUser(user);
        vote.setSession(session);
        vote.setCreatedAt(LocalDateTime.now());

        return vote;
    }

    public Vote updateEntity(UpdateVoteData voteData, Vote vote) {
        vote.setVoteOption(voteData.voteOption());
        return vote;
    }

    public VoteDetailsData toVoteDetails(Vote vote) {
        return new VoteDetailsData(vote.getId(),vote.getVoteOption(),
                userMapper.toUserDetails(vote.getUser()), sessionMapper.toSessionDetails(vote.getSession()),vote.getCreatedAt());
    }
}
