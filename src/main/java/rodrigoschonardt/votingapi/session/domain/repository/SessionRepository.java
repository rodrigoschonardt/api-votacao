package rodrigoschonardt.votingapi.session.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rodrigoschonardt.votingapi.session.domain.model.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Page<Session> findAllByTopicId(Long topicId, Pageable pageable);

    void deleteAllByTopicId(Long topicId);
    Integer countAllByTopicId(Long topicId);
}
