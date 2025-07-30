package rodrigoschonardt.votingapi.user.web.mapper;

import org.springframework.stereotype.Component;
import rodrigoschonardt.votingapi.user.domain.model.User;
import rodrigoschonardt.votingapi.user.web.dto.AddUserData;
import rodrigoschonardt.votingapi.user.web.dto.UserDetailsData;

import java.time.LocalDateTime;

@Component
public class UserMapper {
    public User toEntity(AddUserData dto) {
        User user = new User();
        user.setCpf(dto.cpf());
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    public UserDetailsData toUserDetails(User user) {
        return new UserDetailsData(user.getId(), user.getCpf(), user.getCreatedAt());
    }
}
