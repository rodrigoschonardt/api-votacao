package rodrigoschonardt.votingapi.user.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rodrigoschonardt.votingapi.shared.exception.EntityNotFoundException;
import rodrigoschonardt.votingapi.user.domain.external.CpfValidationClient;
import rodrigoschonardt.votingapi.user.domain.external.dto.CpfValidationResponse;
import rodrigoschonardt.votingapi.user.domain.model.User;
import rodrigoschonardt.votingapi.user.domain.repository.UserRepository;
import rodrigoschonardt.votingapi.user.web.dto.AddUserData;
import rodrigoschonardt.votingapi.user.web.mapper.UserMapper;
import rodrigoschonardt.votingapi.shared.exception.EntityAlreadyExistsException;

@Service
public class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CpfValidationClient cpfValidationClient;

    public UserService(UserRepository userRepository, UserMapper userMapper, CpfValidationClient cpfValidationClient) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.cpfValidationClient = cpfValidationClient;
    }

    public User add(AddUserData userData) {
        if (userRepository.existsByCpf(userData.cpf())) {
            LOG.warn("User creation failed - CPF {} already exists", userData.cpf());
            throw new EntityAlreadyExistsException("User", "CPF " + userData.cpf());
        }

        User user = userMapper.toEntity(userData);

        user = userRepository.save(user);

        LOG.info("User created successfully with ID: {}", user.getId());

        return user;
    }

    public void delete(Long id) {
        get(id);
        userRepository.deleteById(id);
        LOG.info("User deleted successfully with ID: {}", id);
    }

    public User get(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", "ID " + id));
    }

    public void validateCpf(String cpf) {
        if (cpfValidationClient.validate(cpf)
                .status().equals(CpfValidationResponse.UNABLE)) {
            throw new EntityNotFoundException("CPF", "CPF " + cpf);
        }
    }
}
