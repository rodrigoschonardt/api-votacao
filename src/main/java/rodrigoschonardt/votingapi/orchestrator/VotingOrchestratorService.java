package rodrigoschonardt.votingapi.orchestrator;

import org.springframework.stereotype.Service;
import rodrigoschonardt.votingapi.session.domain.service.SessionService;
import rodrigoschonardt.votingapi.topic.domain.service.TopicService;

@Service
public class VotingOrchestratorService {
    private final TopicService topicService;
    private final SessionService sessionService;

    public VotingOrchestratorService(TopicService topicService, SessionService sessionService) {
        this.topicService = topicService;
        this.sessionService = sessionService;
    }

    public void deleteTopicAndSessions(Long topicId) {
        sessionService.deleteAllByTopic(topicId);
        topicService.delete(topicId);
    }
}
