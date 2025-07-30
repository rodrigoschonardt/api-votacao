package rodrigoschonardt.votingapi.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rodrigoschonardt.votingapi.user.domain.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByCpf(String cpf);
}
