package rodrigoschonardt.votingapi.session.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionMapper sessionMapper;

    @Mock
    private TopicService topicService;

    @InjectMocks
    private SessionService sessionService;

    @Test
    void shouldAddSessionSuccessfully() {
        Long topicId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusMinutes(1);
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(2);
        AddSessionData sessionData = new AddSessionData(topicId, startTime, 2);

        Topic topic = new Topic();
        topic.setId(topicId);
        topic.setTitle("Test Topic");

        Session sessionToSave = new Session();
        sessionToSave.setTopic(topic);
        sessionToSave.setStartTime(startTime);
        sessionToSave.setEndTime(endTime);

        Session savedSession = new Session();
        savedSession.setId(1L);
        savedSession.setTopic(topic);
        savedSession.setStartTime(startTime);
        savedSession.setEndTime(endTime);

        when(topicService.get(topicId)).thenReturn(topic);
        when(sessionMapper.toEntity(sessionData, topic)).thenReturn(sessionToSave);
        when(sessionRepository.save(any(Session.class))).thenReturn(savedSession);

        Session result = sessionService.add(sessionData);

        assertNotNull(result);
        verify(topicService).get(topicId);
        verify(sessionMapper).toEntity(sessionData, topic);
        verify(sessionRepository).save(sessionToSave);
    }

    @Test
    void shouldThrowExceptionWhenAddingSessionForNonExistentTopic() {
        Long topicId = 99L;
        AddSessionData sessionData = new AddSessionData(topicId, LocalDateTime.now(), 1);

        when(topicService.get(topicId)).thenThrow(new EntityNotFoundException("Topic", "ID " + topicId));

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> sessionService.add(sessionData)
        );

        assertTrue(exception.getMessage().contains("Topic"));
        assertTrue(exception.getMessage().contains("ID " + topicId));
        verify(topicService).get(topicId);
        verify(sessionMapper, never()).toEntity(any(), any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void shouldDeleteSessionWhenExists() {
        Long sessionId = 1L;
        Session existingSession = new Session();
        existingSession.setId(sessionId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(existingSession));
        doNothing().when(sessionRepository).deleteById(sessionId);

        sessionService.delete(sessionId);

        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository).deleteById(sessionId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentSession() {
        Long sessionId = 99L;
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> sessionService.delete(sessionId)
        );

        assertTrue(exception.getMessage().contains("Session"));
        assertTrue(exception.getMessage().contains("ID " + sessionId));
        verify(sessionRepository).findById(sessionId);
        verify(sessionRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldDeleteAllSessionsByTopicWhenTopicExists() {
        Long topicId = 1L;
        Topic topic = new Topic();
        topic.setId(topicId);

        when(topicService.get(topicId)).thenReturn(topic);
        doNothing().when(sessionRepository).deleteAllByTopicId(topicId);

        sessionService.deleteAllByTopic(topicId);

        verify(topicService).get(topicId);
        verify(sessionRepository).deleteAllByTopicId(topicId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingAllSessionsForNonExistentTopic() {
        Long topicId = 99L;
        when(topicService.get(topicId)).thenThrow(new EntityNotFoundException("Topic", "ID " + topicId));

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> sessionService.deleteAllByTopic(topicId)
        );

        assertTrue(exception.getMessage().contains("Topic"));
        assertTrue(exception.getMessage().contains("ID " + topicId));
        verify(topicService).get(topicId);
        verify(sessionRepository, never()).deleteAllByTopicId(anyLong());
    }

    @Test
    void shouldReturnSessionWhenIdExists() {
        Long sessionId = 1L;
        Session session = new Session();
        session.setId(sessionId);
        session.setStartTime(LocalDateTime.now());

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        Session result = sessionService.get(sessionId);

        assertNotNull(result);
        assertEquals(sessionId, result.getId());
        verify(sessionRepository).findById(sessionId);
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFound() {
        Long sessionId = 99L;
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> sessionService.get(sessionId)
        );

        assertTrue(exception.getMessage().contains("Session"));
        assertTrue(exception.getMessage().contains("ID " + sessionId));
        verify(sessionRepository).findById(sessionId);
    }

    @Test
    void shouldReturnPageOfSessionsByTopic() {
        Long topicId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Topic topic = new Topic();
        topic.setId(topicId);

        Session session1 = new Session();
        session1.setId(10L);
        session1.setTopic(topic);
        Session session2 = new Session();
        session2.setId(11L);
        session2.setTopic(topic);

        List<Session> sessionsList = List.of(session1, session2);
        Page<Session> sessionPage = new PageImpl<>(sessionsList, pageable, sessionsList.size());

        when(topicService.get(topicId)).thenReturn(topic);
        when(sessionRepository.findAllByTopicId(topicId, pageable)).thenReturn(sessionPage);

        Page<Session> result = sessionService.getAllByTopic(topicId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(session1.getId(), result.getContent().get(0).getId());
        assertEquals(session2.getId(), result.getContent().get(1).getId());
        verify(topicService).get(topicId);
        verify(sessionRepository).findAllByTopicId(topicId, pageable);
    }

    @Test
    void shouldThrowExceptionWhenGettingSessionsByNonExistentTopic() {
        Long topicId = 99L;
        Pageable pageable = PageRequest.of(0, 10);

        when(topicService.get(topicId)).thenThrow(new EntityNotFoundException("Topic", "ID " + topicId));

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> sessionService.getAllByTopic(topicId, pageable)
        );

        assertTrue(exception.getMessage().contains("Topic"));
        assertTrue(exception.getMessage().contains("ID " + topicId));
        verify(topicService).get(topicId);
        verify(sessionRepository, never()).findAllByTopicId(anyLong(), any());
    }

    @Test
    void shouldReturnCorrectCountByTopic() {
        Long topicId = 1L;
        when(sessionRepository.countAllByTopicId(topicId)).thenReturn(5);

        Integer count = sessionService.countByTopic(topicId);

        assertEquals(5, count);
        verify(sessionRepository).countAllByTopicId(topicId);
    }

    @Test
    void shouldReturnTrueWhenVotingIsOpen() {
        Session session = new Session();
        session.setStartTime(LocalDateTime.now().minusMinutes(5));
        session.setEndTime(LocalDateTime.now().plusMinutes(5));

        boolean isOpen = sessionService.isVotingOpen(session);

        assertTrue(isOpen);
    }

    @Test
    void shouldReturnFalseWhenVotingIsNotYetOpen() {
        Session session = new Session();
        session.setStartTime(LocalDateTime.now().plusMinutes(5));
        session.setEndTime(LocalDateTime.now().plusMinutes(10));

        boolean isOpen = sessionService.isVotingOpen(session);

        assertFalse(isOpen);
    }

    @Test
    void shouldReturnFalseWhenVotingIsClosed() {
        Session session = new Session();
        session.setStartTime(LocalDateTime.now().minusMinutes(10));
        session.setEndTime(LocalDateTime.now().minusMinutes(5));

        boolean isOpen = sessionService.isVotingOpen(session);

        assertFalse(isOpen);
    }

    @Test
    void shouldUpdateSessionSuccessfully() {
        Long sessionId = 1L;
        UpdateSessionData updateData = new UpdateSessionData(sessionId, LocalDateTime.now().plusMinutes(5), 5);

        Session existingSession = new Session();
        existingSession.setId(sessionId);
        existingSession.setStartTime(LocalDateTime.now().plusMinutes(1));
        existingSession.setEndTime(LocalDateTime.now().plusMinutes(2));

        Session updatedSessionFromMapper = new Session();
        updatedSessionFromMapper.setId(sessionId);
        updatedSessionFromMapper.setStartTime(LocalDateTime.now().plusMinutes(5));
        updatedSessionFromMapper.setEndTime(LocalDateTime.now().plusMinutes(10));

        Session savedSession = new Session();
        savedSession.setId(sessionId);
        savedSession.setStartTime(LocalDateTime.now().plusMinutes(5));
        savedSession.setEndTime(LocalDateTime.now().plusMinutes(10));

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(existingSession));
        when(sessionMapper.updateEntity(updateData, existingSession)).thenReturn(updatedSessionFromMapper);
        when(sessionRepository.save(updatedSessionFromMapper)).thenReturn(savedSession);

        Session result = sessionService.update(updateData);

        assertNotNull(result);
        assertEquals(sessionId, result.getId());
        verify(sessionRepository).findById(sessionId);
        verify(sessionMapper).updateEntity(updateData, existingSession);
        verify(sessionRepository).save(updatedSessionFromMapper);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentSession() {
        Long sessionId = 99L;
        UpdateSessionData updateData = new UpdateSessionData(sessionId, LocalDateTime.now().plusMinutes(5), 5);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> sessionService.update(updateData)
        );

        assertTrue(exception.getMessage().contains("Session"));
        assertTrue(exception.getMessage().contains("ID " + sessionId));
        verify(sessionRepository).findById(sessionId);
        verify(sessionMapper, never()).updateEntity(any(), any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingOpenSession() {
        Long sessionId = 1L;
        UpdateSessionData updateData = new UpdateSessionData(sessionId, LocalDateTime.now().plusMinutes(5), 5);

        Session openSession = new Session();
        openSession.setId(sessionId);
        openSession.setStartTime(LocalDateTime.now().minusMinutes(5));
        openSession.setEndTime(LocalDateTime.now().plusMinutes(5));

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(openSession));

        InvalidSessionStateException exception = assertThrows(
                InvalidSessionStateException.class,
                () -> sessionService.update(updateData)
        );

        assertTrue(exception.getMessage().contains("open"));
        verify(sessionRepository).findById(sessionId);
        verify(sessionMapper, never()).updateEntity(any(), any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingClosedSession() {
        Long sessionId = 1L;
        UpdateSessionData updateData = new UpdateSessionData(sessionId, LocalDateTime.now().plusMinutes(5), 5);

        Session closedSession = new Session();
        closedSession.setId(sessionId);
        closedSession.setStartTime(LocalDateTime.now().minusMinutes(10));
        closedSession.setEndTime(LocalDateTime.now().minusMinutes(5));

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(closedSession));

        InvalidSessionStateException exception = assertThrows(
                InvalidSessionStateException.class,
                () -> sessionService.update(updateData)
        );

        assertTrue(exception.getMessage().contains("closed"));
        verify(sessionRepository).findById(sessionId);
        verify(sessionMapper, never()).updateEntity(any(), any());
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void shouldReturnTrueWhenVotingIsClosed() {
        Session session = new Session();
        session.setStartTime(LocalDateTime.now().minusMinutes(10));
        session.setEndTime(LocalDateTime.now().minusMinutes(5));

        boolean isClosed = sessionService.isVotingClosed(session);

        assertTrue(isClosed);
    }

    @Test
    void shouldReturnFalseWhenVotingIsNotYetClosed() {
        Session session = new Session();
        session.setStartTime(LocalDateTime.now().minusMinutes(5));
        session.setEndTime(LocalDateTime.now().plusMinutes(5));

        boolean isClosed = sessionService.isVotingClosed(session);

        assertFalse(isClosed);
    }

    @Test
    void shouldReturnFalseWhenVotingHasNotStartedYet() {
        Session session = new Session();
        session.setStartTime(LocalDateTime.now().plusMinutes(5));
        session.setEndTime(LocalDateTime.now().plusMinutes(10));

        boolean isClosed = sessionService.isVotingClosed(session);

        assertFalse(isClosed);
    }

    @Test
    void shouldReturnTrueWhenVotingJustClosed() {
        Session session = new Session();
        session.setStartTime(LocalDateTime.now().minusMinutes(5));
        session.setEndTime(LocalDateTime.now().minusSeconds(1));

        boolean isClosed = sessionService.isVotingClosed(session);

        assertTrue(isClosed);
    }
}
