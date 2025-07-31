package rodrigoschonardt.votingapi.vote.web.controller.v1;

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
import rodrigoschonardt.votingapi.session.web.dto.SessionDetailsData;
import rodrigoschonardt.votingapi.shared.exception.EntityNotFoundException;
import rodrigoschonardt.votingapi.user.web.dto.UserDetailsData;
import rodrigoschonardt.votingapi.vote.domain.model.Vote;
import rodrigoschonardt.votingapi.vote.domain.service.VoteService;
import rodrigoschonardt.votingapi.vote.web.dto.AddVoteData;
import rodrigoschonardt.votingapi.vote.web.dto.UpdateVoteData;
import rodrigoschonardt.votingapi.vote.web.dto.VoteDetailsData;
import rodrigoschonardt.votingapi.vote.web.mapper.VoteMapper;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoteController.class)
@Import(VoteControllerTest.TestConfig.class)
class VoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VoteService voteService;

    @Autowired
    private VoteMapper voteMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public VoteService voteService() {
            return mock(VoteService.class);
        }

        @Bean
        public VoteMapper voteMapper() {
            return mock(VoteMapper.class);
        }
    }

    @Test
    void shouldCreateVoteAndReturn201() throws Exception {
        AddVoteData voteData = new AddVoteData(Vote.VoteOption.YES, 1L, 1L);
        Vote vote = new Vote();
        vote.setId(1L);
        vote.setVoteOption(Vote.VoteOption.YES);
        vote.setCreatedAt(LocalDateTime.now());

        VoteDetailsData voteDetails = new VoteDetailsData(1L, Vote.VoteOption.YES, null, null, LocalDateTime.now());

        when(voteService.add(any(AddVoteData.class))).thenReturn(vote);
        when(voteMapper.toVoteDetails(vote)).thenReturn(voteDetails);

        mockMvc.perform(post("/api/v1/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteData)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.voteOption").value("YES"));

        verify(voteService).add(any(AddVoteData.class));
        verify(voteMapper).toVoteDetails(vote);
    }

    @Test
    void shouldUpdateVoteAndReturn200() throws Exception {
        UpdateVoteData voteData = new UpdateVoteData(1L, Vote.VoteOption.NO);
        Vote vote = new Vote();
        vote.setId(1L);
        vote.setVoteOption(Vote.VoteOption.NO);
        vote.setCreatedAt(LocalDateTime.now());

        VoteDetailsData voteDetails = new VoteDetailsData(1L, Vote.VoteOption.NO, null, null, LocalDateTime.now());

        when(voteService.update(any(UpdateVoteData.class))).thenReturn(vote);
        when(voteMapper.toVoteDetails(vote)).thenReturn(voteDetails);

        mockMvc.perform(put("/api/v1/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.voteOption").value("NO"));

        verify(voteService, atLeastOnce()).update(any(UpdateVoteData.class));
        verify(voteMapper).toVoteDetails(vote);
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentVote() throws Exception {
        UpdateVoteData voteData = new UpdateVoteData(99L, Vote.VoteOption.NO);
        when(voteService.update(any(UpdateVoteData.class)))
                .thenThrow(new EntityNotFoundException("Vote", "ID " + 99L));

        mockMvc.perform(put("/api/v1/votes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voteData)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(voteService).update(any(UpdateVoteData.class));
    }

    @Test
    void shouldDeleteVoteAndReturn204() throws Exception {
        Long voteId = 1L;
        doNothing().when(voteService).delete(voteId);

        mockMvc.perform(delete("/api/v1/votes/{id}", voteId))
                .andExpect(status().isNoContent());

        verify(voteService).delete(voteId);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentVote() throws Exception {
        Long voteId = 99L;
        doThrow(new EntityNotFoundException("Vote", "ID " + voteId))
                .when(voteService).delete(voteId);

        mockMvc.perform(delete("/api/v1/votes/{id}", voteId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(voteService).delete(voteId);
    }

    @Test
    void shouldGetVoteAndReturn200() throws Exception {
        Long voteId = 1L;
        Vote vote = new Vote();
        vote.setId(voteId);
        vote.setVoteOption(Vote.VoteOption.YES);
        vote.setCreatedAt(LocalDateTime.now());

        VoteDetailsData voteDetails = new VoteDetailsData(voteId, Vote.VoteOption.YES, null, null, LocalDateTime.now());
        when(voteService.get(voteId)).thenReturn(vote);
        when(voteMapper.toVoteDetails(vote)).thenReturn(voteDetails);

        mockMvc.perform(get("/api/v1/votes/{id}", voteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(voteId))
                .andExpect(jsonPath("$.voteOption").value("YES"));

        verify(voteService).get(voteId);
        verify(voteMapper).toVoteDetails(vote);
    }

    @Test
    void shouldReturn404WhenGettingNonExistentVote() throws Exception {
        Long voteId = 99L;
        when(voteService.get(voteId)).thenThrow(new EntityNotFoundException("Vote", "ID " + voteId));

        mockMvc.perform(get("/api/v1/votes/{id}", voteId))
                .andExpect(status().isNotFound());

        verify(voteService).get(voteId);
    }

    @Test
    void shouldGetAllVotesBySessionAndReturn200() throws Exception {
        Long sessionId = 1L;
        Vote vote = new Vote();
        vote.setId(1L);
        vote.setVoteOption(Vote.VoteOption.YES);
        vote.setCreatedAt(LocalDateTime.now());

        VoteDetailsData voteDetails = new VoteDetailsData(1L, Vote.VoteOption.YES, null, null, LocalDateTime.now());

        Page<Vote> votePage = new PageImpl<>(Collections.singletonList(vote));

        when(voteService.getAllBySession(eq(sessionId), any(PageRequest.class))).thenReturn(votePage);
        when(voteMapper.toVoteDetails(vote)).thenReturn(voteDetails);

        mockMvc.perform(get("/api/v1/votes/session/{sessionId}?page=0&size=10", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(voteService).getAllBySession(eq(sessionId), any(PageRequest.class));
    }

    @Test
    void shouldReturn404WhenGettingVotesByNonExistentSession() throws Exception {
        Long sessionId = 99L;
        when(voteService.getAllBySession(eq(sessionId), any(PageRequest.class)))
                .thenThrow(new EntityNotFoundException("Session", "ID " + sessionId));

        mockMvc.perform(get("/api/v1/votes/session/{sessionId}?page=0&size=10", sessionId))
                .andExpect(status().isNotFound());

        verify(voteService).getAllBySession(eq(sessionId), any(PageRequest.class));
    }
}