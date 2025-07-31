package rodrigoschonardt.votingapi.user.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rodrigoschonardt.votingapi.shared.exception.EntityAlreadyExistsException;
import rodrigoschonardt.votingapi.shared.exception.EntityNotFoundException;
import rodrigoschonardt.votingapi.user.domain.external.CpfValidationClient;
import rodrigoschonardt.votingapi.user.domain.external.dto.CpfValidationResponse;
import rodrigoschonardt.votingapi.user.domain.model.User;
import rodrigoschonardt.votingapi.user.domain.repository.UserRepository;
import rodrigoschonardt.votingapi.user.web.dto.AddUserData;
import rodrigoschonardt.votingapi.user.web.mapper.UserMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    CpfValidationClient cpfValidationClient;
    @Mock
    UserMapper userMapper;
    @InjectMocks
    UserService userService;

    @Test
    void shouldCreateUserWhenCpfDoesNotExist() {
        AddUserData userData = new AddUserData("123.456.789-11");
        User user = new User();
        user.setId(1L);
        user.setCpf("123.456.789-11");

        when(userRepository.existsByCpf("123.456.789-11")).thenReturn(false);
        when(userMapper.toEntity(userData)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.add(userData);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("123.456.789-11", result.getCpf());
        verify(userRepository).existsByCpf("123.456.789-11");
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenCpfAlreadyExists() {
        AddUserData userData = new AddUserData("123.456.789-11");
        when(userRepository.existsByCpf("123.456.789-11")).thenReturn(true);

        EntityAlreadyExistsException exception = assertThrows(
                EntityAlreadyExistsException.class,
                () -> userService.add(userData)
        );

        assertTrue(exception.getMessage().contains("CPF 123.456.789-11"));
        verify(userRepository).existsByCpf("123.456.789-11");
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReturnUserWhenIdExists() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setCpf("123.456.789-11");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.get(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.get(userId)
        );

        assertTrue(exception.getMessage().contains("ID 1"));
        verify(userRepository).findById(userId);
    }

    @Test
    void shouldDeleteUserWhenExists() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.delete(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.delete(userId));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).deleteById(userId);
    }

    @Test
    void shouldValidateCpfSuccessfully() {
        String cpf = "123.456.789-11";
        CpfValidationResponse response = new CpfValidationResponse(CpfValidationResponse.ABLE);
        when(cpfValidationClient.validate(cpf)).thenReturn(response);

        assertDoesNotThrow(() -> userService.validateCpf(cpf));
        verify(cpfValidationClient).validate(cpf);
    }

    @Test
    void shouldThrowExceptionWhenCpfValidationFails() {
        String cpf = "123.456.789-11";
        CpfValidationResponse response = new CpfValidationResponse(CpfValidationResponse.UNABLE);
        when(cpfValidationClient.validate(cpf)).thenReturn(response);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.validateCpf(cpf)
        );

        assertTrue(exception.getMessage().contains("CPF 123.456.789-11"));
        verify(cpfValidationClient).validate(cpf);
    }
}