package rodrigoschonardt.votingapi.topic.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddTopicData(@NotBlank @Size(max = 255) String title, @Size(max = 4000) String description ) {
}
