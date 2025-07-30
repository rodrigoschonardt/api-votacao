package rodrigoschonardt.votingapi.topic.web.dto;

public record TopicResultsData(Long id, String title, String description, Integer sessionsCount,
                               Integer votesYesCount, Integer votesNoCount, Integer yesPercentage) {
}
