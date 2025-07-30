package rodrigoschonardt.votingapi.vote.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rodrigoschonardt.votingapi.vote.domain.model.Vote;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByUserIdAndSessionId(Long userId, Long sessionId);
    void deleteAllBySession_Topic_Id(Long sessionTopicId);
    void deleteAllBySessionId(Long sessionId);
    Page<Vote> findAllBySessionId(Long sessionId, Pageable pageable);
}
