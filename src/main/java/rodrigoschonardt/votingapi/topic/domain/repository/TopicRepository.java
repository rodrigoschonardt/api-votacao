package rodrigoschonardt.votingapi.topic.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rodrigoschonardt.votingapi.topic.domain.model.Topic;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
}
