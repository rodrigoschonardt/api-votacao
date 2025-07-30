package rodrigoschonardt.votingapi.session.web.dto;

import rodrigoschonardt.votingapi.topic.web.dto.TopicDetailsData;

import java.time.LocalDateTime;

public record SessionDetailsData(Long id, TopicDetailsData topic, LocalDateTime startTime,
                                 LocalDateTime endTime, LocalDateTime createdAt) {
}
