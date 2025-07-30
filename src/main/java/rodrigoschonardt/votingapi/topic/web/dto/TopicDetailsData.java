package rodrigoschonardt.votingapi.topic.web.dto;

import java.time.LocalDateTime;

public record TopicDetailsData(Long id, String title, String description, LocalDateTime createdAt) {
}
