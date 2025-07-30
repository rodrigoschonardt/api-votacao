package rodrigoschonardt.votingapi.orchestrator;

import org.springframework.stereotype.Service;
import rodrigoschonardt.votingapi.session.domain.service.SessionService;
import rodrigoschonardt.votingapi.topic.domain.model.Topic;
import rodrigoschonardt.votingapi.topic.domain.service.TopicService;
import rodrigoschonardt.votingapi.topic.web.dto.TopicResultsData;
import rodrigoschonardt.votingapi.vote.domain.model.Vote;
import rodrigoschonardt.votingapi.vote.domain.service.VoteService;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

    public TopicResultsData getTopicResults(Long topicId) {
        Topic topic = topicService.get(topicId);

        Integer countSessions = sessionService.countByTopic(topicId);

        Integer countYes = voteService.countByTopicAndOption(topicId, Vote.VoteOption.YES);
        Integer countNo = voteService.countByTopicAndOption(topicId, Vote.VoteOption.NO);

        Integer totalVotes = countYes + countNo;
        Integer yesPercentage = totalVotes > 0 ?
                BigDecimal.valueOf(countYes * 100.0 / totalVotes)
                        .setScale(0, RoundingMode.HALF_UP)
                        .intValue() : 0;

        return new TopicResultsData( topicId, topic.getTitle(), topic.getDescription(),
                countSessions, countYes, countNo, yesPercentage );
    }
}
