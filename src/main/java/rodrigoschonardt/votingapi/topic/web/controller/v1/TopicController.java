package rodrigoschonardt.votingapi.topic.web.controller.v1;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import rodrigoschonardt.votingapi.topic.domain.model.Topic;
import rodrigoschonardt.votingapi.topic.domain.service.TopicService;
import rodrigoschonardt.votingapi.topic.web.dto.AddTopicData;
import rodrigoschonardt.votingapi.topic.web.dto.TopicDetailsData;
import rodrigoschonardt.votingapi.topic.web.dto.UpdateTopicData;
import rodrigoschonardt.votingapi.topic.web.mapper.TopicMapper;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/topics")
public class TopicController {
    private final TopicService topicService;
    private final TopicMapper topicMapper;

    public TopicController(TopicService topicService, TopicMapper topicMapper) {
        this.topicService = topicService;
        this.topicMapper = topicMapper;
    }

    @PostMapping
    public ResponseEntity<TopicDetailsData> add(@RequestBody @Valid AddTopicData topicData, UriComponentsBuilder uriBuilder) {
        Topic topic = topicService.add(topicData);

        URI uri = uriBuilder.path("/api/v1/topics/{id}").buildAndExpand(topic.getId()).toUri();

        return ResponseEntity.created(uri).body(topicMapper.toTopicDetails(topic));
    }

    @PutMapping
    public ResponseEntity<TopicDetailsData> update(@RequestBody @Valid UpdateTopicData topicData) {
        Topic topic = topicService.update(topicData);

        return ResponseEntity.ok(topicMapper.toTopicDetails(topic));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable Long id) {
        topicService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TopicDetailsData> get(@PathVariable Long id) {
        Topic topic = topicService.get(id);

        return ResponseEntity.ok(topicMapper.toTopicDetails(topic));
    }

    @GetMapping
    public ResponseEntity<Page<TopicDetailsData>> getAll(Pageable page) {
        Page<Topic> topics = topicService.getAll(page);

        return ResponseEntity.ok(topics.map(topicMapper::toTopicDetails));
    }
}
