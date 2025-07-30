package rodrigoschonardt.votingapi.session.web.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AddSessionData(@NotNull Long topicId, @FutureOrPresent LocalDateTime startTime, Integer duration) {
}
