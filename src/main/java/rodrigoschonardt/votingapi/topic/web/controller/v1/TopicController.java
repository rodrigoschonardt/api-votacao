package rodrigoschonardt.votingapi.topic.web.controller.v1;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import rodrigoschonardt.votingapi.orchestrator.VotingOrchestratorService;
import rodrigoschonardt.votingapi.topic.domain.model.Topic;
import rodrigoschonardt.votingapi.topic.domain.service.TopicService;
import rodrigoschonardt.votingapi.topic.web.dto.AddTopicData;
import rodrigoschonardt.votingapi.topic.web.dto.TopicDetailsData;
import rodrigoschonardt.votingapi.topic.web.dto.TopicResultsData;
import rodrigoschonardt.votingapi.topic.web.dto.UpdateTopicData;
import rodrigoschonardt.votingapi.topic.web.mapper.TopicMapper;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/topics")
public class TopicController {
    private final TopicService topicService;
    private final TopicMapper topicMapper;
    private final VotingOrchestratorService orchestratorService;

    public TopicController(TopicService topicService, TopicMapper topicMapper, VotingOrchestratorService orchestratorService) {
        this.topicService = topicService;
        this.topicMapper = topicMapper;
        this.orchestratorService = orchestratorService;
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
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orchestratorService.deleteTopicSessionsAndVotes(id);

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

    @GetMapping("/result/{id}")
    public ResponseEntity<TopicResultsData> getResults(@PathVariable Long id) {
        TopicResultsData results = orchestratorService.getTopicResults(id);

        return ResponseEntity.ok(results);
    }
}
