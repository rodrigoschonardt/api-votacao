package rodrigoschonardt.votingapi.shared.exception;

public class VotingNotAllowedException extends RuntimeException {
    public VotingNotAllowedException(String message) {
        super(message);
    }
}
