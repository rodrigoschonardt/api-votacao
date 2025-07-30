package rodrigoschonardt.votingapi.session.web.mapper;

import org.springframework.stereotype.Component;
import rodrigoschonardt.votingapi.session.domain.model.Session;
import rodrigoschonardt.votingapi.session.web.dto.AddSessionData;
import rodrigoschonardt.votingapi.session.web.dto.SessionDetailsData;
import rodrigoschonardt.votingapi.topic.domain.model.Topic;
import rodrigoschonardt.votingapi.topic.web.mapper.TopicMapper;

import java.time.LocalDateTime;

@Component
public class SessionMapper {
    private final TopicMapper topicMapper;

    public SessionMapper(TopicMapper topicMapper) {
        this.topicMapper = topicMapper;
    }

    public Session toEntity(AddSessionData sessionData, Topic topic) {
        Session session = new Session();

        session.setCreatedAt(LocalDateTime.now());
        session.setStartTime(sessionData.startTime() == null ? LocalDateTime.now() : sessionData.startTime());
        session.setEndTime(session.getStartTime().plusMinutes(sessionData.duration() == null ? 1 : sessionData.duration()));
        session.setTopic(topic);

        return session;
    }

    public SessionDetailsData toSessionDetails(Session session) {
        return new SessionDetailsData(session.getId(), topicMapper.toTopicDetails(session.getTopic()),
                session.getStartTime(), session.getEndTime(), session.getCreatedAt());
    }
}
