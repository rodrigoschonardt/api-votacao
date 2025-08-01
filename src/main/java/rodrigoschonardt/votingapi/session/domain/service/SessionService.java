package rodrigoschonardt.votingapi.session.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rodrigoschonardt.votingapi.session.domain.model.Session;
import rodrigoschonardt.votingapi.session.domain.repository.SessionRepository;
import rodrigoschonardt.votingapi.session.web.dto.AddSessionData;
import rodrigoschonardt.votingapi.session.web.dto.UpdateSessionData;
import rodrigoschonardt.votingapi.session.web.mapper.SessionMapper;
import rodrigoschonardt.votingapi.shared.exception.EntityNotFoundException;
import rodrigoschonardt.votingapi.shared.exception.InvalidSessionStateException;
import rodrigoschonardt.votingapi.topic.domain.model.Topic;
import rodrigoschonardt.votingapi.topic.domain.service.TopicService;

import java.time.LocalDateTime;

@Service
public class SessionService {
    private final static Logger LOG = LoggerFactory.getLogger(SessionService.class);
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final TopicService topicService;

    public SessionService(SessionRepository sessionRepository, SessionMapper sessionMapper, TopicService topicService) {
        this.sessionRepository = sessionRepository;
        this.sessionMapper = sessionMapper;
        this.topicService = topicService;
    }

    public Session add(AddSessionData sessionData) {
        Topic topic = topicService.get(sessionData.topicId());

        Session session = sessionMapper.toEntity(sessionData, topic);

        session = sessionRepository.save(session);

        LOG.info("Session added successfully with ID: {}", session.getId());

        return session;
    }

    public void delete(Long id) {
        get(id);

        sessionRepository.deleteById(id);

        LOG.info("Session deleted successfully with ID: {}", id);
    }

    public Session update(UpdateSessionData sessionData)
    {
        Session session = get(sessionData.id());

        if (isVotingOpen(session)) {
            throw new InvalidSessionStateException(session.getId(), "open");
        }

        if (isVotingClosed(session)) {
            throw new InvalidSessionStateException(session.getId(), "closed");
        }

        session = sessionMapper.updateEntity(sessionData, session);

        session = sessionRepository.save(session);

        LOG.info("Session updated successfully with ID: {}", session.getId());

        return session;
    }

    public void deleteAllByTopic(Long topicId) {
        topicService.get(topicId);

        sessionRepository.deleteAllByTopicId(topicId);

        LOG.info("Sessions deleted successfully with topic ID: {}", topicId);
    }

    public Session get(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Session", "ID " + id));
    }

    public Page<Session> getAllByTopic(Long topicId, Pageable pageable) {
        Topic topic = topicService.get(topicId);

        Page<Session> sessions = sessionRepository.findAllByTopicId(topicId, pageable);

        return sessions;
    }

    public Integer countByTopic(Long topicId) {
        return sessionRepository.countAllByTopicId(topicId);
    }

    public boolean isVotingOpen(Session session) {
        // Adição da injeção de Clock para testes
        LocalDateTime now = LocalDateTime.now();

        // Talvez adicionar uma margem de erro em caso de latência mais alta
        return !now.isBefore(session.getStartTime()) && !now.isAfter(session.getEndTime());
    }

    public boolean isVotingClosed(Session session) {
        // Adição da injeção de Clock para testes
        LocalDateTime now = LocalDateTime.now();

        return now.isAfter(session.getEndTime());
    }
}
