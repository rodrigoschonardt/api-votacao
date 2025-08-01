package rodrigoschonardt.votingapi.session.web.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rodrigoschonardt.votingapi.orchestrator.VotingOrchestratorService;
import rodrigoschonardt.votingapi.session.domain.model.Session;
import rodrigoschonardt.votingapi.session.domain.service.SessionService;
import rodrigoschonardt.votingapi.session.web.dto.AddSessionData;
import rodrigoschonardt.votingapi.session.web.dto.SessionDetailsData;
import rodrigoschonardt.votingapi.session.web.dto.UpdateSessionData;
import rodrigoschonardt.votingapi.session.web.mapper.SessionMapper;
import rodrigoschonardt.votingapi.shared.exception.EntityNotFoundException;
import rodrigoschonardt.votingapi.shared.exception.InvalidSessionStateException;
import rodrigoschonardt.votingapi.topic.domain.model.Topic;
import rodrigoschonardt.votingapi.topic.web.dto.TopicDetailsData;

import java.time.LocalDateTime;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionController.class)
@Import(SessionControllerTest.TestConfig.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private VotingOrchestratorService votingOrchestratorService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public SessionService sessionService() {
            return mock(SessionService.class);
        }

        @Bean
        public SessionMapper sessionMapper() {
            return mock(SessionMapper.class);
        }

        @Bean
        public VotingOrchestratorService votingOrchestratorService() {
            return mock(VotingOrchestratorService.class);
        }
    }

    @Test
    void shouldCreateSessionAndReturn201() throws Exception {
        Long topicId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusMinutes(1);
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(3);
        AddSessionData addSessionData = new AddSessionData(topicId, startTime, 2);

        Topic topic = new Topic();
        topic.setId(topicId);
        topic.setTitle("Mocked Topic Title");
        topic.setDescription("Mocked Topic Description");
        topic.setCreatedAt(LocalDateTime.now());

        Session createdSession = new Session();
        createdSession.setId(10L);
        createdSession.setTopic(topic);
        createdSession.setStartTime(startTime);
        createdSession.setEndTime(endTime);
        createdSession.setCreatedAt(LocalDateTime.now());

        SessionDetailsData sessionDetailsData = getSessionDetailsData(topic, createdSession);

        when(sessionService.add(any(AddSessionData.class))).thenReturn(createdSession);
        when(sessionMapper.toSessionDetails(createdSession)).thenReturn(sessionDetailsData);

        mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addSessionData)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(10L));

        verify(sessionService, atLeastOnce()).add(any(AddSessionData.class));
    }

    private static SessionDetailsData getSessionDetailsData(Topic topic, Session createdSession) {
        TopicDetailsData topicDetailsData = new TopicDetailsData(
                topic.getId(),
                topic.getTitle(),
                topic.getDescription(),
                topic.getCreatedAt()
        );

        SessionDetailsData sessionDetailsData = new SessionDetailsData(
                createdSession.getId(),
                topicDetailsData,
                createdSession.getStartTime(),
                createdSession.getEndTime(),
                createdSession.getCreatedAt()
        );
        return sessionDetailsData;
    }

    @Test
    void shouldReturn400WhenCreatingSessionWithInvalidData() throws Exception {
        // Data de início no passado
        AddSessionData invalidSessionData = new AddSessionData(null, LocalDateTime.now().minusHours(1), 1);

        mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSessionData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenCreatingSessionForNonExistentTopic() throws Exception {
        Long nonExistentTopicId = 99L;
        AddSessionData addSessionData = new AddSessionData(nonExistentTopicId, LocalDateTime.now().plusHours(1), 1);

        when(sessionService.add(any(AddSessionData.class)))
                .thenThrow(new EntityNotFoundException("Topic", "ID " + nonExistentTopicId));

        mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addSessionData)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(sessionService).add(any(AddSessionData.class));
    }

    @Test
    void shouldDeleteSessionAndReturn204() throws Exception {
        Long sessionId = 1L;
        doNothing().when(votingOrchestratorService).deleteSessionAndVotes(sessionId);

        mockMvc.perform(delete("/api/v1/sessions/{id}", sessionId))
                .andExpect(status().isNoContent());

        verify(votingOrchestratorService).deleteSessionAndVotes(sessionId);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentSession() throws Exception {
        Long sessionId = 99L;
        doThrow(new EntityNotFoundException("Session", "ID " + sessionId))
                .when(votingOrchestratorService).deleteSessionAndVotes(sessionId);

        mockMvc.perform(delete("/api/v1/sessions/{id}", sessionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(votingOrchestratorService).deleteSessionAndVotes(sessionId);
    }

    @Test
    void shouldGetSessionByIdAndReturn200() throws Exception {
        Long sessionId = 1L;
        Topic topic = new Topic();
        topic.setId(100L);
        topic.setTitle("Fetched Topic Title");
        topic.setDescription("Fetched Topic Description");
        topic.setCreatedAt(LocalDateTime.now());

        Session session = new Session();
        session.setId(sessionId);
        session.setTopic(topic);
        session.setStartTime(LocalDateTime.now());
        session.setEndTime(LocalDateTime.now().plusHours(1));
        session.setCreatedAt(LocalDateTime.now());

        SessionDetailsData sessionDetailsData = getSessionDetailsData(topic, session);

        when(sessionService.get(sessionId)).thenReturn(session);
        when(sessionMapper.toSessionDetails(session)).thenReturn(sessionDetailsData);

        mockMvc.perform(get("/api/v1/sessions/{id}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId));

        verify(sessionService).get(sessionId);
        verify(sessionMapper).toSessionDetails(session);
    }

    @Test
    void shouldReturn404WhenGettingNonExistentSession() throws Exception {
        Long sessionId = 99L;
        when(sessionService.get(sessionId)).thenThrow(new EntityNotFoundException("Session", "ID " + sessionId));

        mockMvc.perform(get("/api/v1/sessions/{id}", sessionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(sessionService).get(sessionId);
    }

    @Test
    void shouldGetAllSessionsByTopicAndReturn200() throws Exception {
        Long topicId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        Topic topic = new Topic();
        topic.setId(topicId);
        topic.setTitle("Topic for Sessions");
        topic.setDescription("Description for Topic for Sessions");
        topic.setCreatedAt(LocalDateTime.now().minusDays(3));

        Session session1 = new Session();
        session1.setId(1L);
        session1.setTopic(topic);
        session1.setStartTime(LocalDateTime.now().minusHours(2));
        session1.setEndTime(LocalDateTime.now().minusHours(1));
        session1.setCreatedAt(LocalDateTime.now());

        Session session2 = new Session();
        session2.setId(2L);
        session2.setTopic(topic);
        session2.setStartTime(LocalDateTime.now().plusHours(1));
        session2.setEndTime(LocalDateTime.now().plusHours(2));
        session2.setCreatedAt(LocalDateTime.now());

        Page<Session> sessionPage = new PageImpl<>(List.of(session1, session2), pageable, 2);

        SessionDetailsData sessionDetailsData1 = getSessionDetailsData(topic, session1);
        SessionDetailsData sessionDetailsData2 = getSessionDetailsData(topic, session2);

        when(sessionService.getAllByTopic(eq(topicId), any(Pageable.class))).thenReturn(sessionPage);
        when(sessionMapper.toSessionDetails(session1)).thenReturn(sessionDetailsData1);
        when(sessionMapper.toSessionDetails(session2)).thenReturn(sessionDetailsData2);

        mockMvc.perform(get("/api/v1/sessions/topic/{topicId}?page=0&size=10", topicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[1].id").value(2L));

        verify(sessionService).getAllByTopic(eq(topicId), any(Pageable.class));
    }

    @Test
    void shouldReturn404WhenGettingAllSessionsByNonExistentTopic() throws Exception {
        Long nonExistentTopicId = 99L;
        when(sessionService.getAllByTopic(eq(nonExistentTopicId), any(Pageable.class)))
                .thenThrow(new EntityNotFoundException("Topic", "ID " + nonExistentTopicId));

        mockMvc.perform(get("/api/v1/sessions/topic/{topicId}?page=0&size=10", nonExistentTopicId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(sessionService).getAllByTopic(eq(nonExistentTopicId), any(Pageable.class));
    }

    @Test
    void shouldUpdateSessionAndReturn200() throws Exception {
        Long sessionId = 1L;
        LocalDateTime newStartTime = LocalDateTime.now().plusMinutes(10);
        UpdateSessionData updateSessionData = new UpdateSessionData(sessionId, newStartTime, 5);

        Topic topic = new Topic();
        topic.setId(100L);
        topic.setTitle("Updated Topic Title");
        topic.setDescription("Updated Topic Description");
        topic.setCreatedAt(LocalDateTime.now().minusDays(1));

        Session updatedSession = new Session();
        updatedSession.setId(sessionId);
        updatedSession.setTopic(topic);
        updatedSession.setStartTime(newStartTime);
        updatedSession.setEndTime(newStartTime.plusMinutes(5));
        updatedSession.setCreatedAt(LocalDateTime.now().minusHours(1));

        SessionDetailsData sessionDetailsData = getSessionDetailsData(topic, updatedSession);

        when(sessionService.update(any(UpdateSessionData.class))).thenReturn(updatedSession);
        when(sessionMapper.toSessionDetails(updatedSession)).thenReturn(sessionDetailsData);

        mockMvc.perform(put("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSessionData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId))
                .andExpect(jsonPath("$.startTime").exists())
                .andExpect(jsonPath("$.endTime").exists());

        verify(sessionService, atLeastOnce()).update(any(UpdateSessionData.class));
        verify(sessionMapper).toSessionDetails(updatedSession);
    }

    @Test
    void shouldReturn400WhenUpdatingSessionWithInvalidData() throws Exception {
        UpdateSessionData invalidUpdateData = new UpdateSessionData(null, LocalDateTime.now().minusHours(1), 1);

        mockMvc.perform(put("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateData)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentSession() throws Exception {
        Long nonExistentSessionId = 99L;
        UpdateSessionData updateSessionData = new UpdateSessionData(nonExistentSessionId, LocalDateTime.now().plusHours(1), 2);

        when(sessionService.update(any(UpdateSessionData.class)))
                .thenThrow(new EntityNotFoundException("Session", "ID " + nonExistentSessionId));

        mockMvc.perform(put("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSessionData)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn409WhenUpdatingOpenSession() throws Exception {
        Long sessionId = 1L;
        UpdateSessionData updateSessionData = new UpdateSessionData(sessionId, LocalDateTime.now().plusHours(1), 3);

        when(sessionService.update(any(UpdateSessionData.class)))
                .thenThrow(new InvalidSessionStateException(sessionId, "open"));

        mockMvc.perform(put("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSessionData)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn409WhenUpdatingClosedSession() throws Exception {
        Long sessionId = 1L;
        UpdateSessionData updateSessionData = new UpdateSessionData(sessionId, LocalDateTime.now().plusHours(1), 3);

        when(sessionService.update(any(UpdateSessionData.class)))
                .thenThrow(new InvalidSessionStateException(sessionId, "closed"));

        mockMvc.perform(put("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateSessionData)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());

    }
}
