package rodrigoschonardt.votingapi.vote.domain.service;

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
import rodrigoschonardt.votingapi.session.domain.service.SessionService;
import rodrigoschonardt.votingapi.shared.exception.EntityAlreadyExistsException;
import rodrigoschonardt.votingapi.shared.exception.EntityNotFoundException;
import rodrigoschonardt.votingapi.shared.exception.VotingNotAllowedException;
import rodrigoschonardt.votingapi.topic.domain.model.Topic;
import rodrigoschonardt.votingapi.topic.domain.service.TopicService;
import rodrigoschonardt.votingapi.user.domain.model.User;
import rodrigoschonardt.votingapi.user.domain.service.UserService;
import rodrigoschonardt.votingapi.vote.domain.model.Vote;
import rodrigoschonardt.votingapi.vote.domain.repository.VoteRepository;
import rodrigoschonardt.votingapi.vote.web.dto.AddVoteData;
import rodrigoschonardt.votingapi.vote.web.dto.UpdateVoteData;
import rodrigoschonardt.votingapi.vote.web.mapper.VoteMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoteServiceTest {

    @Mock
    private VoteRepository voteRepository;
    @Mock
    private VoteMapper voteMapper;
    @Mock
    private TopicService topicService;
    @Mock
    private SessionService sessionService;
    @Mock
    private UserService userService;

    @InjectMocks
    private VoteService voteService;

    @Test
    void shouldAddVoteSuccessfully() {
        Long sessionId = 1L;
        Long userId = 10L;
        AddVoteData voteData = new AddVoteData(Vote.VoteOption.YES, sessionId, userId);

        Session session = new Session();
        session.setId(sessionId);
        session.setStartTime(LocalDateTime.now().minusMinutes(5));
        session.setEndTime(LocalDateTime.now().plusMinutes(5));

        User user = new User();
        user.setId(userId);

        Vote voteToSave = new Vote();
        voteToSave.setSession(session);
        voteToSave.setUser(user);
        voteToSave.setVoteOption(Vote.VoteOption.YES);

        Vote savedVote = new Vote();
        savedVote.setId(100L);
        savedVote.setSession(session);
        savedVote.setUser(user);
        savedVote.setVoteOption(Vote.VoteOption.YES);

        when(sessionService.get(anyLong())).thenReturn(session);
        when(sessionService.isVotingOpen(any(Session.class))).thenReturn(true);
        when(userService.get(anyLong())).thenReturn(user);
        when(voteRepository.existsByUserIdAndSessionId(userId, sessionId)).thenReturn(false);
        when(voteMapper.toEntity(voteData, user, session)).thenReturn(voteToSave);
        when(voteRepository.save(any(Vote.class))).thenReturn(savedVote);

        Vote result = voteService.add(voteData);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(Vote.VoteOption.YES, result.getVoteOption());
        verify(sessionService).get(anyLong());
        verify(sessionService).isVotingOpen(any(Session.class));
        verify(userService).get(anyLong());
        verify(voteRepository).existsByUserIdAndSessionId(userId, sessionId);
        verify(voteMapper).toEntity(voteData, user, session);
        verify(voteRepository).save(voteToSave);
    }

    @Test
    void shouldThrowVotingNotAllowedExceptionWhenSessionIsNotOpenOnAdd() {
        Long sessionId = 1L;
        Long userId = 10L;
        AddVoteData voteData = new AddVoteData(Vote.VoteOption.YES, sessionId, userId);

        Session session = new Session();
        session.setId(sessionId);
        session.setStartTime(LocalDateTime.now().plusHours(1));
        session.setEndTime(LocalDateTime.now().plusHours(2));

        when(sessionService.get(anyLong())).thenReturn(session);
        when(sessionService.isVotingOpen(any(Session.class))).thenReturn(false);

        VotingNotAllowedException exception = assertThrows(
                VotingNotAllowedException.class,
                () -> voteService.add(voteData)
        );

        assertEquals("Session is not open!", exception.getMessage());
        verify(sessionService).get(anyLong());
        verify(sessionService).isVotingOpen(any(Session.class));
        verify(userService, never()).get(anyLong());
        verify(voteRepository, never()).existsByUserIdAndSessionId(anyLong(), anyLong());
        verify(voteMapper, never()).toEntity(any(), any(), any());
        verify(voteRepository, never()).save(any());
    }

    @Test
    void shouldThrowEntityAlreadyExistsExceptionWhenVoteAlreadyExists() {
        Long sessionId = 1L;
        Long userId = 10L;
        AddVoteData voteData = new AddVoteData(Vote.VoteOption.YES, sessionId, userId);

        Session session = new Session();
        session.setId(sessionId);
        session.setStartTime(LocalDateTime.now().minusMinutes(5));
        session.setEndTime(LocalDateTime.now().plusMinutes(5));

        User user = new User();
        user.setId(userId);

        when(sessionService.get(anyLong())).thenReturn(session);
        when(sessionService.isVotingOpen(any(Session.class))).thenReturn(true);
        when(userService.get(anyLong())).thenReturn(user);
        when(voteRepository.existsByUserIdAndSessionId(userId, sessionId)).thenReturn(true);

        EntityAlreadyExistsException exception = assertThrows(
                EntityAlreadyExistsException.class,
                () -> voteService.add(voteData)
        );

        assertTrue(exception.getMessage().contains("Vote"));
        assertTrue(exception.getMessage().contains("User " + userId + " in session " + sessionId));
        verify(sessionService).get(anyLong());
        verify(sessionService).isVotingOpen(any(Session.class));
        verify(userService).get(anyLong());
        verify(voteRepository).existsByUserIdAndSessionId(userId, sessionId);
        verify(voteMapper, never()).toEntity(any(), any(), any());
        verify(voteRepository, never()).save(any());
    }

    @Test
    void shouldUpdateVoteSuccessfully() {
        Long voteId = 1L;
        UpdateVoteData voteData = new UpdateVoteData(voteId, Vote.VoteOption.NO);

        Session session = new Session();
        session.setId(10L);
        session.setStartTime(LocalDateTime.now().minusMinutes(5));
        session.setEndTime(LocalDateTime.now().plusMinutes(5));

        Vote existingVote = new Vote();
        existingVote.setId(voteId);
        existingVote.setVoteOption(Vote.VoteOption.YES);
        existingVote.setSession(session);

        Vote updatedVote = new Vote();
        updatedVote.setId(voteId);
        updatedVote.setVoteOption(Vote.VoteOption.NO);
        updatedVote.setSession(session);

        when(voteRepository.findById(voteId)).thenReturn(Optional.of(existingVote));
        when(sessionService.isVotingOpen(any(Session.class))).thenReturn(true);
        when(voteMapper.updateEntity(voteData, existingVote)).thenReturn(updatedVote);
        when(voteRepository.save(any(Vote.class))).thenReturn(updatedVote);

        Vote result = voteService.update(voteData);

        assertNotNull(result);
        assertEquals(voteId, result.getId());
        assertEquals(Vote.VoteOption.NO, result.getVoteOption());
        verify(voteRepository).findById(voteId);
        verify(sessionService).isVotingOpen(any(Session.class));
        verify(voteMapper).updateEntity(voteData, existingVote);
        verify(voteRepository).save(updatedVote);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenUpdatingNonExistentVote() {
        Long voteId = 99L;
        UpdateVoteData voteData = new UpdateVoteData(voteId, Vote.VoteOption.NO);

        when(voteRepository.findById(voteId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> voteService.update(voteData)
        );

        assertTrue(exception.getMessage().contains("Vote"));
        assertTrue(exception.getMessage().contains("ID " + voteId));
        verify(voteRepository).findById(voteId);
        verify(sessionService, never()).isVotingOpen(any(Session.class));
        verify(voteMapper, never()).updateEntity(any(), any());
        verify(voteRepository, never()).save(any());
    }

    @Test
    void shouldThrowVotingNotAllowedExceptionWhenSessionIsNotOpenOnUpdate() {
        Long voteId = 1L;
        UpdateVoteData voteData = new UpdateVoteData(voteId, Vote.VoteOption.NO);

        Session session = new Session();
        session.setId(10L);
        session.setStartTime(LocalDateTime.now().plusHours(1));
        session.setEndTime(LocalDateTime.now().plusHours(2));

        Vote existingVote = new Vote();
        existingVote.setId(voteId);
        existingVote.setVoteOption(Vote.VoteOption.YES);
        existingVote.setSession(session);

        when(voteRepository.findById(voteId)).thenReturn(Optional.of(existingVote));
        when(sessionService.isVotingOpen(any(Session.class))).thenReturn(false);

        VotingNotAllowedException exception = assertThrows(
                VotingNotAllowedException.class,
                () -> voteService.update(voteData)
        );

        assertEquals("Session is not open!", exception.getMessage());
        verify(voteRepository).findById(voteId);
        verify(sessionService).isVotingOpen(any(Session.class));
        verify(voteMapper, never()).updateEntity(any(), any());
        verify(voteRepository, never()).save(any());
    }

    @Test
    void shouldDeleteVoteSuccessfully() {
        Long voteId = 1L;
        Vote existingVote = new Vote();
        existingVote.setId(voteId);

        when(voteRepository.findById(voteId)).thenReturn(Optional.of(existingVote));
        doNothing().when(voteRepository).deleteById(voteId);

        voteService.delete(voteId);

        verify(voteRepository).findById(voteId);
        verify(voteRepository).deleteById(voteId);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenDeletingNonExistentVote() {
        Long voteId = 99L;
        when(voteRepository.findById(voteId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> voteService.delete(voteId)
        );

        assertTrue(exception.getMessage().contains("Vote"));
        assertTrue(exception.getMessage().contains("ID " + voteId));
        verify(voteRepository).findById(voteId);
        verify(voteRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldDeleteAllVotesByTopicSuccessfully() {
        Long topicId = 1L;
        Topic topic = new Topic();
        topic.setId(topicId);

        when(topicService.get(topicId)).thenReturn(topic);
        doNothing().when(voteRepository).deleteAllBySession_Topic_Id(topicId);

        voteService.deleteAllByTopic(topicId);

        verify(topicService).get(topicId);
        verify(voteRepository).deleteAllBySession_Topic_Id(topicId);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenDeletingAllVotesByNonExistentTopic() {
        Long topicId = 99L;
        when(topicService.get(topicId)).thenThrow(new EntityNotFoundException("Topic", "ID " + topicId));

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> voteService.deleteAllByTopic(topicId)
        );

        assertTrue(exception.getMessage().contains("Topic"));
        assertTrue(exception.getMessage().contains("ID " + topicId));
        verify(topicService).get(topicId);
        verify(voteRepository, never()).deleteAllBySession_Topic_Id(anyLong());
    }

    @Test
    void shouldDeleteAllVotesBySessionSuccessfully() {
        Long sessionId = 1L;
        Session session = new Session();
        session.setId(sessionId);

        when(sessionService.get(anyLong())).thenReturn(session);
        doNothing().when(voteRepository).deleteAllBySessionId(sessionId);

        voteService.deleteAllBySession(sessionId);

        verify(sessionService).get(anyLong());
        verify(voteRepository).deleteAllBySessionId(sessionId);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenDeletingAllVotesByNonExistentSession() {
        Long sessionId = 99L;
        when(sessionService.get(anyLong())).thenThrow(new EntityNotFoundException("Session", "ID " + sessionId));

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> voteService.deleteAllBySession(sessionId)
        );

        assertTrue(exception.getMessage().contains("Session"));
        assertTrue(exception.getMessage().contains("ID " + sessionId));
        verify(sessionService).get(anyLong());
        verify(voteRepository, never()).deleteAllBySessionId(anyLong());
    }

    @Test
    void shouldGetVoteByIdSuccessfully() {
        Long voteId = 1L;
        Vote vote = new Vote();
        vote.setId(voteId);
        vote.setVoteOption(Vote.VoteOption.YES);

        when(voteRepository.findById(voteId)).thenReturn(Optional.of(vote));

        Vote result = voteService.get(voteId);

        assertNotNull(result);
        assertEquals(voteId, result.getId());
        verify(voteRepository).findById(voteId);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenGettingNonExistentVote() {
        Long voteId = 99L;
        when(voteRepository.findById(voteId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> voteService.get(voteId)
        );

        assertTrue(exception.getMessage().contains("Vote"));
        assertTrue(exception.getMessage().contains("ID " + voteId));
        verify(voteRepository).findById(voteId);
    }

    @Test
    void shouldGetAllVotesBySessionSuccessfully() {
        Long sessionId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Session session = new Session();
        session.setId(sessionId);

        Vote vote1 = new Vote();
        vote1.setId(10L);
        vote1.setSession(session);
        Vote vote2 = new Vote();
        vote2.setId(11L);
        vote2.setSession(session);

        List<Vote> votesList = List.of(vote1, vote2);
        Page<Vote> votePage = new PageImpl<>(votesList, pageable, votesList.size());

        when(sessionService.get(anyLong())).thenReturn(session);
        when(voteRepository.findAllBySessionId(sessionId, pageable)).thenReturn(votePage);

        Page<Vote> result = voteService.getAllBySession(sessionId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(vote1.getId(), result.getContent().get(0).getId());
        verify(sessionService).get(anyLong());
        verify(voteRepository).findAllBySessionId(sessionId, pageable);
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenGettingAllVotesByNonExistentSession() {
        Long sessionId = 99L;
        Pageable pageable = PageRequest.of(0, 10);

        when(sessionService.get(anyLong())).thenThrow(new EntityNotFoundException("Session", "ID " + sessionId));

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> voteService.getAllBySession(sessionId, pageable)
        );

        assertTrue(exception.getMessage().contains("Session"));
        assertTrue(exception.getMessage().contains("ID " + sessionId));
        verify(sessionService).get(anyLong());
        verify(voteRepository, never()).findAllBySessionId(anyLong(), any());
    }

    @Test
    void shouldCountByTopicAndOptionSuccessfully() {
        Long topicId = 1L;
        Vote.VoteOption option = Vote.VoteOption.YES;
        when(voteRepository.countAllByVoteOptionAndSession_Topic_Id(option, topicId)).thenReturn(5);

        Integer count = voteService.countByTopicAndOption(topicId, option);

        assertEquals(5, count);
        verify(voteRepository).countAllByVoteOptionAndSession_Topic_Id(option, topicId);
    }
}
