package rodrigoschonardt.votingapi.topic.web.mapper;

import org.springframework.stereotype.Component;
import rodrigoschonardt.votingapi.topic.domain.model.Topic;
import rodrigoschonardt.votingapi.topic.web.dto.AddTopicData;
import rodrigoschonardt.votingapi.topic.web.dto.TopicDetailsData;
import rodrigoschonardt.votingapi.topic.web.dto.UpdateTopicData;

import java.time.LocalDateTime;

@Component
public class TopicMapper {
    public Topic toEntity(AddTopicData topicData) {
        Topic topic = new Topic();

        topic.setTitle(topicData.title());
        topic.setDescription(topicData.description() != null ? topicData.description() : "");
        topic.setCreatedAt(LocalDateTime.now());
        return topic;
    }

    public Topic updateEntity(UpdateTopicData topicData, Topic topic) {
        topic.setTitle(topicData.title());
        topic.setDescription(topicData.description() != null ? topicData.description() : "");

        return topic;
    }

    public TopicDetailsData toTopicDetails(Topic topic) {
        return new TopicDetailsData(topic.getId(), topic.getTitle(), topic.getDescription(), topic.getCreatedAt());
    }
}
