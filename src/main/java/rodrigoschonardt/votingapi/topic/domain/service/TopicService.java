package rodrigoschonardt.votingapi.topic.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rodrigoschonardt.votingapi.shared.exception.EntityNotFoundException;
import rodrigoschonardt.votingapi.topic.domain.model.Topic;
import rodrigoschonardt.votingapi.topic.domain.repository.TopicRepository;
import rodrigoschonardt.votingapi.topic.web.dto.AddTopicData;
import rodrigoschonardt.votingapi.topic.web.dto.UpdateTopicData;
import rodrigoschonardt.votingapi.topic.web.mapper.TopicMapper;

@Service
public class TopicService {
    private static final Logger LOG = LoggerFactory.getLogger(TopicService.class);
    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;

    public TopicService(TopicRepository topicRepository, TopicMapper topicMapper) {
        this.topicRepository = topicRepository;
        this.topicMapper = topicMapper;
    }

    public Topic add(AddTopicData topicData) {
        Topic topic = topicMapper.toEntity(topicData);

        topic = topicRepository.save(topic);

        LOG.info("Topic created successfully with ID: {}", topic.getId());

        return topic;
    }

    public Topic update(UpdateTopicData topicData) {
        Topic topic = get(topicData.id());

        topic = topicMapper.updateEntity(topicData, topic);

        topic = topicRepository.save(topic);

        LOG.info("Topic updated successfully with ID: {}", topic.getId());

        return topic;
    }

    public void delete(Long id) {
        get(id);

        topicRepository.deleteById(id);

        LOG.info("Topic deleted successfully with ID: {}", id);
    }

    public Topic get(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Topic", "ID " + id));
    }

    public Page<Topic> getAll(Pageable page) {
        return topicRepository.findAll(page);
    }
}
