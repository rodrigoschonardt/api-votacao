package rodrigoschonardt.votingapi.topic.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import rodrigoschonardt.votingapi.shared.exception.EntityNotFoundException;
import rodrigoschonardt.votingapi.topic.domain.model.Topic;
import rodrigoschonardt.votingapi.topic.domain.repository.TopicRepository;
import rodrigoschonardt.votingapi.topic.web.dto.AddTopicData;
import rodrigoschonardt.votingapi.topic.web.dto.UpdateTopicData;
import rodrigoschonardt.votingapi.topic.web.mapper.TopicMapper;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TopicServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TopicMapper topicMapper;

    @InjectMocks
    private TopicService topicService;

    @Test
    void shouldAddTopicSuccessfully() {
        AddTopicData topicData = new AddTopicData("New Topic", "Description of the new topic");
        Topic topicToSave = new Topic();
        topicToSave.setTitle("New Topic");
        topicToSave.setDescription("Description of the new topic");

        Topic savedTopic = new Topic();
        savedTopic.setId(1L);
        savedTopic.setTitle("New Topic");
        savedTopic.setDescription("Description of the new topic");

        when(topicMapper.toEntity(topicData)).thenReturn(topicToSave);
        when(topicRepository.save(any(Topic.class))).thenReturn(savedTopic);

        Topic result = topicService.add(topicData);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Topic", result.getTitle());
        verify(topicMapper).toEntity(topicData);
        verify(topicRepository).save(topicToSave);
    }

    @Test
    void shouldUpdateTopicWhenExists() {
        Long topicId = 1L;
        UpdateTopicData topicData = new UpdateTopicData(topicId, "Updated Title", "Updated Description");
        Topic existingTopic = new Topic();
        existingTopic.setId(topicId);
        existingTopic.setTitle("Old Title");

        Topic updatedTopic = new Topic();
        updatedTopic.setId(topicId);
        updatedTopic.setTitle("Updated Title");
        updatedTopic.setDescription("Updated Description");

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(existingTopic));
        when(topicMapper.updateEntity(topicData, existingTopic)).thenReturn(updatedTopic);
        when(topicRepository.save(any(Topic.class))).thenReturn(updatedTopic);

        Topic result = topicService.update(topicData);

        assertNotNull(result);
        assertEquals(topicId, result.getId());
        assertEquals("Updated Title", result.getTitle());
        verify(topicRepository).findById(topicId);
        verify(topicMapper).updateEntity(topicData, existingTopic);
        verify(topicRepository).save(updatedTopic);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentTopic() {
        Long topicId = 1L;
        UpdateTopicData topicData = new UpdateTopicData(topicId, "Title", "Description");
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> topicService.update(topicData)
        );

        assertTrue(exception.getMessage().contains("ID " + topicId));
        verify(topicRepository).findById(topicId);
        verify(topicMapper, never()).updateEntity(any(), any());
        verify(topicRepository, never()).save(any());
    }

    @Test
    void shouldDeleteTopicWhenExists() {
        Long topicId = 1L;
        Topic existingTopic = new Topic();
        existingTopic.setId(topicId);

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(existingTopic));
        doNothing().when(topicRepository).deleteById(topicId);

        topicService.delete(topicId);

        verify(topicRepository).findById(topicId);
        verify(topicRepository).deleteById(topicId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentTopic() {
        Long topicId = 1L;
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> topicService.delete(topicId));
        verify(topicRepository).findById(topicId);
        verify(topicRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldReturnTopicWhenIdExists() {
        Long topicId = 1L;
        Topic topic = new Topic();
        topic.setId(topicId);
        topic.setTitle("Test Topic");

        when(topicRepository.findById(topicId)).thenReturn(Optional.of(topic));

        Topic result = topicService.get(topicId);

        assertNotNull(result);
        assertEquals(topicId, result.getId());
        verify(topicRepository).findById(topicId);
    }

    @Test
    void shouldThrowExceptionWhenTopicNotFound() {
        Long topicId = 1L;
        when(topicRepository.findById(topicId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> topicService.get(topicId)
        );

        assertTrue(exception.getMessage().contains("ID " + topicId));
        verify(topicRepository).findById(topicId);
    }

    @Test
    void shouldReturnPageOfTopics() {
        Pageable pageable = PageRequest.of(0, 10);
        Topic topic = new Topic();
        topic.setId(1L);
        Page<Topic> topicPage = new PageImpl<>(Collections.singletonList(topic), pageable, 1);

        when(topicRepository.findAll(pageable)).thenReturn(topicPage);

        Page<Topic> result = topicService.getAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(topic.getId(), result.getContent().get(0).getId());
        verify(topicRepository).findAll(pageable);
    }
}