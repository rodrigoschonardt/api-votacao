package rodrigoschonardt.votingapi.orchestrator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rodrigoschonardt.votingapi.session.domain.service.SessionService;
import rodrigoschonardt.votingapi.shared.exception.EntityNotFoundException;
import rodrigoschonardt.votingapi.topic.domain.model.Topic;
import rodrigoschonardt.votingapi.topic.domain.service.TopicService;
import rodrigoschonardt.votingapi.topic.web.dto.TopicResultsData;
import rodrigoschonardt.votingapi.vote.domain.model.Vote;
import rodrigoschonardt.votingapi.vote.domain.service.VoteService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotingOrchestratorServiceTest {

    @Mock
    private TopicService topicService;

    @Mock
    private SessionService sessionService;

    @Mock
    private VoteService voteService;

    @InjectMocks
    private VotingOrchestratorService orchestratorService;

    private Topic topic;

    @BeforeEach
    void setUp() {
        topic = new Topic();
        topic.setId(1L);
        topic.setTitle("Test Topic");
        topic.setDescription("Test Description");
        topic.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldDeleteTopicSessionsAndVotesSuccessfully() {
        Long topicId = 1L;

        orchestratorService.deleteTopicSessionsAndVotes(topicId);

        verify(voteService).deleteAllByTopic(topicId);
        verify(sessionService).deleteAllByTopic(topicId);
        verify(topicService).delete(topicId);
    }

    @Test
    void shouldThrowExceptionWhenTopicNotFoundDuringDeletion() {
        Long topicId = 99L;
        doThrow(new EntityNotFoundException("Topic", "ID " + topicId))
                .when(topicService).delete(topicId);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orchestratorService.deleteTopicSessionsAndVotes(topicId)
        );

        assertTrue(exception.getMessage().contains("Topic"));
        assertTrue(exception.getMessage().contains("ID " + topicId));

        verify(voteService).deleteAllByTopic(topicId);
        verify(sessionService).deleteAllByTopic(topicId);
        verify(topicService).delete(topicId);
    }

    @Test
    void shouldDeleteSessionAndVotesSuccessfully() {
        Long sessionId = 1L;

        orchestratorService.deleteSessionAndVotes(sessionId);

        verify(voteService).deleteAllBySession(sessionId);
        verify(sessionService).delete(sessionId);
    }

    @Test
    void shouldThrowExceptionWhenSessionNotFoundDuringDeletion() {
        Long sessionId = 99L;
        doThrow(new EntityNotFoundException("Session", "ID " + sessionId))
                .when(sessionService).delete(sessionId);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orchestratorService.deleteSessionAndVotes(sessionId)
        );

        assertTrue(exception.getMessage().contains("Session"));
        assertTrue(exception.getMessage().contains("ID " + sessionId));

        verify(voteService).deleteAllBySession(sessionId);
        verify(sessionService).delete(sessionId);
    }

    @Test
    void shouldGetTopicResultsWithVotes() {
        Long topicId = 1L;
        Integer sessionsCount = 2;
        Integer yesVotes = 7;
        Integer noVotes = 3;
        Integer expectedYesPercentage = 70; // 7/10 * 100 = 70%

        when(topicService.get(topicId)).thenReturn(topic);
        when(sessionService.countByTopic(topicId)).thenReturn(sessionsCount);
        when(voteService.countByTopicAndOption(topicId, Vote.VoteOption.YES)).thenReturn(yesVotes);
        when(voteService.countByTopicAndOption(topicId, Vote.VoteOption.NO)).thenReturn(noVotes);

        TopicResultsData result = orchestratorService.getTopicResults(topicId);

        assertNotNull(result);
        assertEquals(topicId, result.id());
        assertEquals(topic.getTitle(), result.title());
        assertEquals(topic.getDescription(), result.description());
        assertEquals(sessionsCount, result.sessionsCount());
        assertEquals(yesVotes, result.votesYesCount());
        assertEquals(noVotes, result.votesNoCount());
        assertEquals(expectedYesPercentage, result.yesPercentage());

        verify(topicService).get(topicId);
        verify(sessionService).countByTopic(topicId);
        verify(voteService).countByTopicAndOption(topicId, Vote.VoteOption.YES);
        verify(voteService).countByTopicAndOption(topicId, Vote.VoteOption.NO);
    }

    @Test
    void shouldGetTopicResultsWithNoVotes() {
        Long topicId = 1L;
        Integer sessionsCount = 1;
        Integer yesVotes = 0;
        Integer noVotes = 0;
        Integer expectedYesPercentage = 0;

        when(topicService.get(topicId)).thenReturn(topic);
        when(sessionService.countByTopic(topicId)).thenReturn(sessionsCount);
        when(voteService.countByTopicAndOption(topicId, Vote.VoteOption.YES)).thenReturn(yesVotes);
        when(voteService.countByTopicAndOption(topicId, Vote.VoteOption.NO)).thenReturn(noVotes);

        TopicResultsData result = orchestratorService.getTopicResults(topicId);

        assertNotNull(result);
        assertEquals(topicId, result.id());
        assertEquals(topic.getTitle(), result.title());
        assertEquals(topic.getDescription(), result.description());
        assertEquals(sessionsCount, result.sessionsCount());
        assertEquals(yesVotes, result.votesYesCount());
        assertEquals(noVotes, result.votesNoCount());
        assertEquals(expectedYesPercentage, result.yesPercentage());
    }

    @Test
    void shouldCalculateYesPercentageCorrectlyWithRounding() {
        Long topicId = 1L;
        Integer sessionsCount = 1;
        Integer yesVotes = 2;
        Integer noVotes = 1;
        Integer expectedYesPercentage = 67; // 2/3 * 100 = 66.67 -> 67

        when(topicService.get(topicId)).thenReturn(topic);
        when(sessionService.countByTopic(topicId)).thenReturn(sessionsCount);
        when(voteService.countByTopicAndOption(topicId, Vote.VoteOption.YES)).thenReturn(yesVotes);
        when(voteService.countByTopicAndOption(topicId, Vote.VoteOption.NO)).thenReturn(noVotes);

        TopicResultsData result = orchestratorService.getTopicResults(topicId);

        assertEquals(expectedYesPercentage, result.yesPercentage());
    }

    @Test
    void shouldCalculateYesPercentageWithExactDivision() {
        Long topicId = 1L;
        Integer sessionsCount = 1;
        Integer yesVotes = 1;
        Integer noVotes = 1;
        Integer expectedYesPercentage = 50; // 1/2 * 100 = 50%

        when(topicService.get(topicId)).thenReturn(topic);
        when(sessionService.countByTopic(topicId)).thenReturn(sessionsCount);
        when(voteService.countByTopicAndOption(topicId, Vote.VoteOption.YES)).thenReturn(yesVotes);
        when(voteService.countByTopicAndOption(topicId, Vote.VoteOption.NO)).thenReturn(noVotes);

        TopicResultsData result = orchestratorService.getTopicResults(topicId);

        assertEquals(expectedYesPercentage, result.yesPercentage());
    }

    @Test
    void shouldCalculateYesPercentageWithAllYesVotes() {
        Long topicId = 1L;
        Integer sessionsCount = 1;
        Integer yesVotes = 10;
        Integer noVotes = 0;
        Integer expectedYesPercentage = 100;

        when(topicService.get(topicId)).thenReturn(topic);
        when(sessionService.countByTopic(topicId)).thenReturn(sessionsCount);
        when(voteService.countByTopicAndOption(topicId, Vote.VoteOption.YES)).thenReturn(yesVotes);
        when(voteService.countByTopicAndOption(topicId, Vote.VoteOption.NO)).thenReturn(noVotes);

        TopicResultsData result = orchestratorService.getTopicResults(topicId);

        assertEquals(expectedYesPercentage, result.yesPercentage());
    }

    @Test
    void shouldCalculateYesPercentageWithAllNoVotes() {
        Long topicId = 1L;
        Integer sessionsCount = 1;
        Integer yesVotes = 0;
        Integer noVotes = 5;
        Integer expectedYesPercentage = 0;

        when(topicService.get(topicId)).thenReturn(topic);
        when(sessionService.countByTopic(topicId)).thenReturn(sessionsCount);
        when(voteService.countByTopicAndOption(topicId, Vote.VoteOption.YES)).thenReturn(yesVotes);
        when(voteService.countByTopicAndOption(topicId, Vote.VoteOption.NO)).thenReturn(noVotes);

        TopicResultsData result = orchestratorService.getTopicResults(topicId);

        assertEquals(expectedYesPercentage, result.yesPercentage());
    }

    @Test
    void shouldThrowExceptionWhenTopicNotFoundForResults() {
        Long topicId = 99L;
        when(topicService.get(topicId))
                .thenThrow(new EntityNotFoundException("Topic", "ID " + topicId));

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> orchestratorService.getTopicResults(topicId)
        );

        assertTrue(exception.getMessage().contains("Topic"));
        assertTrue(exception.getMessage().contains("ID " + topicId));

        verify(topicService).get(topicId);
        verifyNoInteractions(sessionService, voteService);
    }
}