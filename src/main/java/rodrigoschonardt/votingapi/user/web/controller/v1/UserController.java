package rodrigoschonardt.votingapi.user.web.controller.v1;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import rodrigoschonardt.votingapi.user.domain.model.User;
import rodrigoschonardt.votingapi.user.domain.service.UserService;
import rodrigoschonardt.votingapi.user.web.dto.AddUserData;
import rodrigoschonardt.votingapi.user.web.dto.UserDetailsData;
import rodrigoschonardt.votingapi.user.web.mapper.UserMapper;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @PostMapping
    public ResponseEntity<UserDetailsData> add(@RequestBody @Valid AddUserData userData, UriComponentsBuilder uriBuilder) {
        User user = userService.add(userData);

        URI uri = uriBuilder.path("/api/v1/users/{id}").buildAndExpand(user.getId()).toUri();

        return ResponseEntity.created(uri).body(userMapper.toUserDetails(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsData> get(@PathVariable Long id) {
        User user = userService.get(id);

        return ResponseEntity.ok(userMapper.toUserDetails(user));
    }

    @GetMapping("/{cpf}/validate")
    public ResponseEntity<Void> validateCpf(@PathVariable String cpf) {
        userService.validateCpf(cpf);
        return ResponseEntity.ok().build();
    }
}
