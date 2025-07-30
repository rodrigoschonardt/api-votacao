package rodrigoschonardt.votingapi.orchestrator;

import org.springframework.stereotype.Service;
import rodrigoschonardt.votingapi.session.domain.service.SessionService;
import rodrigoschonardt.votingapi.topic.domain.service.TopicService;
import rodrigoschonardt.votingapi.vote.domain.service.VoteService;

@Service
public class VotingOrchestratorService {
    private final TopicService topicService;
    private final SessionService sessionService;
    private final VoteService voteService;

    public VotingOrchestratorService(TopicService topicService, SessionService sessionService, VoteService voteService) {
        this.topicService = topicService;
        this.sessionService = sessionService;
        this.voteService = voteService;
    }

    public void deleteTopicSessionsAndVotes(Long topicId) {
        voteService.deleteAllByTopic(topicId);
        sessionService.deleteAllByTopic(topicId);
        topicService.delete(topicId);
    }

    public void deleteSessionAndVotes(Long sessionId) {
        voteService.deleteAllBySession(sessionId);
        sessionService.delete(sessionId);
    }
}
