package rodrigoschonardt.votingapi.vote.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rodrigoschonardt.votingapi.session.domain.model.Session;
import rodrigoschonardt.votingapi.session.domain.service.SessionService;
import rodrigoschonardt.votingapi.shared.exception.EntityAlreadyExistsException;
import rodrigoschonardt.votingapi.shared.exception.EntityNotFoundException;
import rodrigoschonardt.votingapi.shared.exception.VotingNotAllowedException;
import rodrigoschonardt.votingapi.topic.domain.service.TopicService;
import rodrigoschonardt.votingapi.user.domain.model.User;
import rodrigoschonardt.votingapi.user.domain.service.UserService;
import rodrigoschonardt.votingapi.vote.domain.model.Vote;
import rodrigoschonardt.votingapi.vote.domain.repository.VoteRepository;
import rodrigoschonardt.votingapi.vote.web.dto.AddVoteData;
import rodrigoschonardt.votingapi.vote.web.dto.UpdateVoteData;
import rodrigoschonardt.votingapi.vote.web.mapper.VoteMapper;

@Service
public class VoteService {
    private final static Logger LOG = LoggerFactory.getLogger(VoteService.class);
    private final VoteRepository voteRepository;
    private final VoteMapper voteMapper;
    private final TopicService topicService;
    private final SessionService sessionService;
    private final UserService userService;

    public VoteService(VoteRepository voteRepository, VoteMapper voteMapper, TopicService topicService,
                       SessionService sessionService, UserService userService) {
        this.voteRepository = voteRepository;
        this.voteMapper = voteMapper;
        this.topicService = topicService;
        this.sessionService = sessionService;
        this.userService = userService;
    }

    public Vote add(AddVoteData voteData) {
        Session session = sessionService.get(voteData.sessionId());

        if (!sessionService.isVotingOpen(session)) {
            throw new VotingNotAllowedException("Session is not open!");
        }

        User user = userService.get(voteData.userId());

        if (voteRepository.existsByUserIdAndSessionId(user.getId(), session.getId())) {
            throw new EntityAlreadyExistsException("Vote", "User " + user.getId() + " in session " + session.getId());
        }

        // Em caso de mais validações seria interessante utilizar o strategy pattern

        Vote vote = voteMapper.toEntity(voteData, user, session);

        vote = voteRepository.save(vote);

        LOG.info("Vote added successfully with ID: {}", vote.getId());

        return vote;
    }


    public Vote update(UpdateVoteData voteData) {
        Vote vote = get(voteData.id());

        Session session = vote.getSession();

        if (!sessionService.isVotingOpen(session)) {
            throw new VotingNotAllowedException("Session is not open!");
        }

        vote = voteMapper.updateEntity(voteData, vote);

        vote = voteRepository.save(vote);

        LOG.info("Vote updated successfully with ID: {}", vote.getId());

        return vote;
    }

    public void delete(Long id) {
        get(id);

        voteRepository.deleteById(id);

        LOG.info("Vote deleted successfully with ID: {}", id);
    }

    public void deleteAllByTopic(Long topicId) {
        topicService.get(topicId);

        voteRepository.deleteAllBySession_Topic_Id(topicId);

        LOG.info("Votes deleted successfully with topic ID: {}", topicId);
    }

    public void deleteAllBySession(Long sessionId) {
        sessionService.get(sessionId);

        voteRepository.deleteAllBySessionId(sessionId);

        LOG.info("Votes deleted successfully with session ID: {}", sessionId);
    }

    public Vote get(Long id) {
        return voteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vote", "ID " + id));
    }

    public Page<Vote> getAllBySession(Long sessionId, Pageable pageable) {
        sessionService.get(sessionId);

        return voteRepository.findAllBySessionId(sessionId, pageable);
    }

    public Integer countByTopicAndOption(Long topicId, Vote.VoteOption option) {
        return voteRepository.countAllByVoteOptionAndSession_Topic_Id(option, topicId);
    }
}
