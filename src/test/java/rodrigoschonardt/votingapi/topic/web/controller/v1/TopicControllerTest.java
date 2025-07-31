package rodrigoschonardt.votingapi.topic.web.controller.v1;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rodrigoschonardt.votingapi.orchestrator.VotingOrchestratorService;
import rodrigoschonardt.votingapi.shared.exception.EntityNotFoundException;
import rodrigoschonardt.votingapi.topic.domain.model.Topic;
import rodrigoschonardt.votingapi.topic.domain.service.TopicService;
import rodrigoschonardt.votingapi.topic.web.dto.AddTopicData;
import rodrigoschonardt.votingapi.topic.web.dto.TopicDetailsData;
import rodrigoschonardt.votingapi.topic.web.dto.TopicResultsData;
import rodrigoschonardt.votingapi.topic.web.dto.UpdateTopicData;
import rodrigoschonardt.votingapi.topic.web.mapper.TopicMapper;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TopicController.class)
@Import(TopicControllerTest.TestConfig.class)
class TopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TopicService topicService;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private VotingOrchestratorService orchestratorService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TopicService topicService() {
            return mock(TopicService.class);
        }

        @Bean
        public TopicMapper topicMapper() {
            return mock(TopicMapper.class);
        }

        @Bean
        public VotingOrchestratorService orchestratorService() {
            return mock(VotingOrchestratorService.class);
        }
    }

    @Test
    void shouldCreateTopicAndReturn201() throws Exception {
        AddTopicData topicData = new AddTopicData("New Topic", "Description");
        Topic topic = new Topic();
        topic.setId(1L);
        topic.setTitle("New Topic");
        topic.setDescription("Description");
        topic.setCreatedAt(LocalDateTime.now());

        TopicDetailsData topicDetails = new TopicDetailsData(1L, "New Topic", "Description", topic.getCreatedAt());

        when(topicService.add(any(AddTopicData.class))).thenReturn(topic);
        when(topicMapper.toTopicDetails(topic)).thenReturn(topicDetails);

        mockMvc.perform(post("/api/v1/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicData)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Topic"));

        verify(topicService).add(any(AddTopicData.class));
        verify(topicMapper).toTopicDetails(topic);
    }

    @Test
    void shouldReturn400WhenCreatingTopicWithInvalidData() throws Exception {
        AddTopicData topicData = new AddTopicData("", "Description");

        mockMvc.perform(post("/api/v1/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicData)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldUpdateTopicAndReturn200() throws Exception {
        UpdateTopicData topicData = new UpdateTopicData(1L, "Updated Topic", "Updated Desc");
        Topic topic = new Topic();
        topic.setId(1L);
        topic.setCreatedAt(LocalDateTime.now());
        TopicDetailsData topicDetails = new TopicDetailsData(1L, "Updated Topic", "Updated Desc", topic.getCreatedAt());

        when(topicService.update(any(UpdateTopicData.class))).thenReturn(topic);
        when(topicMapper.toTopicDetails(topic)).thenReturn(topicDetails);

        mockMvc.perform(put("/api/v1/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Topic"));

        verify(topicService, atLeastOnce()).update(any(UpdateTopicData.class));
        verify(topicMapper).toTopicDetails(topic);
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentTopic() throws Exception {
        UpdateTopicData topicData = new UpdateTopicData(99L, "Non-existent Topic", "Desc");
        when(topicService.update(any(UpdateTopicData.class)))
                .thenThrow(new EntityNotFoundException("Topic", "ID " + 99L));

        mockMvc.perform(put("/api/v1/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicData)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(topicService).update(any(UpdateTopicData.class));
    }

    @Test
    void shouldDeleteTopicAndReturn204() throws Exception {
        Long topicId = 1L;
        doNothing().when(orchestratorService).deleteTopicSessionsAndVotes(topicId);

        mockMvc.perform(delete("/api/v1/topics/{id}", topicId))
                .andExpect(status().isNoContent());

        verify(orchestratorService).deleteTopicSessionsAndVotes(topicId);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentTopic() throws Exception {
        Long topicId = 99L;
        doThrow(new EntityNotFoundException("Topic", "ID " + topicId))
                .when(orchestratorService).deleteTopicSessionsAndVotes(topicId);

        mockMvc.perform(delete("/api/v1/topics/{id}", topicId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(orchestratorService).deleteTopicSessionsAndVotes(topicId);
    }

    @Test
    void shouldGetTopicAndReturn200() throws Exception {
        Long topicId = 1L;
        Topic topic = new Topic();
        topic.setId(topicId);
        topic.setCreatedAt(LocalDateTime.now());
        TopicDetailsData topicDetails = new TopicDetailsData(topicId, "Topic", "Desc", topic.getCreatedAt());

        when(topicService.get(topicId)).thenReturn(topic);
        when(topicMapper.toTopicDetails(topic)).thenReturn(topicDetails);

        mockMvc.perform(get("/api/v1/topics/{id}", topicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(topicId));

        verify(topicService).get(topicId);
        verify(topicMapper).toTopicDetails(topic);
    }

    @Test
    void shouldReturn404WhenGettingNonExistentTopic() throws Exception {
        Long topicId = 99L;
        when(topicService.get(topicId)).thenThrow(new EntityNotFoundException("Topic", "ID " + topicId));

        mockMvc.perform(get("/api/v1/topics/{id}", topicId))
                .andExpect(status().isNotFound());

        verify(topicService).get(topicId);
    }

    @Test
    void shouldGetAllTopicsAndReturn200() throws Exception {
        Topic topic = new Topic();
        topic.setId(1L);
        topic.setCreatedAt(LocalDateTime.now());
        TopicDetailsData topicDetails = new TopicDetailsData(1L, "Topic", "Desc", topic.getCreatedAt());

        Page<Topic> topicPage = new PageImpl<>(Collections.singletonList(topic));

        when(topicService.getAll(any(PageRequest.class))).thenReturn(topicPage);
        when(topicMapper.toTopicDetails(topic)).thenReturn(topicDetails);

        mockMvc.perform(get("/api/v1/topics?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(topicService).getAll(any(PageRequest.class));
    }

    @Test
    void shouldGetTopicResultsAndReturn200() throws Exception {
        Long topicId = 1L;
        TopicResultsData results = new TopicResultsData(topicId, "Topic Title", "Topic Description",
                1, 1, 0, 100);
        when(orchestratorService.getTopicResults(topicId)).thenReturn(results);

        mockMvc.perform(get("/api/v1/topics/result/{id}", topicId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(topicId));

        verify(orchestratorService, atLeastOnce()).getTopicResults(topicId);
    }

    @Test
    void shouldReturn404WhenGettingResultsForNonExistentTopic() throws Exception {
        Long topicId = 99L;
        when(orchestratorService.getTopicResults(topicId))
                .thenThrow(new EntityNotFoundException("Topic", "ID " + topicId));

        mockMvc.perform(get("/api/v1/topics/result/{id}", topicId))
                .andExpect(status().isNotFound());

        verify(orchestratorService).getTopicResults(topicId);
    }
}
