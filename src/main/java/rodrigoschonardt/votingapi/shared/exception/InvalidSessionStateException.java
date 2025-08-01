package rodrigoschonardt.votingapi.shared.exception;

public class SessionAlreadyStartedException extends RuntimeException {
    public SessionAlreadyStartedException(Long sessionId) {
        super("Voting session with ID " + sessionId + " is already open and cannot be modified.");
    }
}
