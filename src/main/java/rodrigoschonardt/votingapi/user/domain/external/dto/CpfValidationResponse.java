package rodrigoschonardt.votingapi.user.domain.external.dto;

public record CpfValidationResponse(String status) {
    public static final String ABLE = "ABLE_TO_VOTE";
    public static final String UNABLE = "UNABLE_TO_VOTE";
}
