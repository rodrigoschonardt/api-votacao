package rodrigoschonardt.votingapi.user.web.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rodrigoschonardt.votingapi.shared.exception.EntityAlreadyExistsException;
import rodrigoschonardt.votingapi.shared.exception.EntityNotFoundException;
import rodrigoschonardt.votingapi.user.domain.model.User;
import rodrigoschonardt.votingapi.user.domain.service.UserService;
import rodrigoschonardt.votingapi.user.web.dto.AddUserData;
import rodrigoschonardt.votingapi.user.web.dto.UserDetailsData;
import rodrigoschonardt.votingapi.user.web.mapper.UserMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public UserMapper userMapper() {
            return mock(UserMapper.class);
        }
    }

    @Test
    void shouldCreateUserAndReturn201() throws Exception {
        AddUserData userData = new AddUserData("123.456.789-11");
        User user = new User();
        user.setId(1L);
        user.setCpf("123.456.789-11");
        user.setCreatedAt(LocalDateTime.now());

        UserDetailsData userDetails = new UserDetailsData(1L, "123.456.789-11", LocalDateTime.now());

        when(userService.add(any(AddUserData.class))).thenReturn(user);
        when(userMapper.toUserDetails(user)).thenReturn(userDetails);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userData)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cpf").value("123.456.789-11"));

        verify(userService, atLeastOnce()).add(any(AddUserData.class));
        verify(userMapper).toUserDetails(user);
    }

    @Test
    void shouldReturn409WhenCpfAlreadyExists() throws Exception {
        AddUserData userData = new AddUserData("123.456.789-11");
        when(userService.add(any(AddUserData.class)))
                .thenThrow(new EntityAlreadyExistsException("User", "CPF 123.456.789-11"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userData)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());

        verify(userService).add(any(AddUserData.class));
    }

    @Test
    void shouldReturn400WhenCpfIsInvalid() throws Exception {
        AddUserData userData = new AddUserData("123");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn400WhenCpfIsNull() throws Exception {
        AddUserData userData = new AddUserData(null);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldGetUserAndReturn200() throws Exception {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setCpf("123.456.789-11");

        UserDetailsData userDetails = new UserDetailsData(userId, "123.456.789-11", LocalDateTime.now());

        when(userService.get(userId)).thenReturn(user);
        when(userMapper.toUserDetails(user)).thenReturn(userDetails);

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.cpf").value("123.456.789-11"));

        verify(userService).get(userId);
        verify(userMapper).toUserDetails(user);
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        Long userId = 1L;
        when(userService.get(userId))
                .thenThrow(new EntityNotFoundException("User", "ID " + userId));

        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(userService, atLeastOnce()).get(userId);
    }

    @Test
    void shouldDeleteUserAndReturn204() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService).delete(userId);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
        Long userId = 1L;
        doThrow(new EntityNotFoundException("User", "ID " + userId))
                .when(userService).delete(userId);

        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(userService, atLeastOnce()).delete(userId);
    }

    @Test
    void shouldValidateCpfAndReturn200() throws Exception {
        String cpf = "123.456.789-11";

        mockMvc.perform(get("/api/v1/users/{cpf}/validate", cpf))
                .andExpect(status().isOk());

        verify(userService).validateCpf(cpf);
    }

    @Test
    void shouldReturn404WhenCpfValidationFails() throws Exception {
        String cpf = "123.456.789-11";
        doThrow(new EntityNotFoundException("CPF", "CPF " + cpf))
                .when(userService).validateCpf(cpf);

        mockMvc.perform(get("/api/v1/users/{cpf}/validate", cpf))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        verify(userService, atLeastOnce()).validateCpf(cpf);
    }
}
