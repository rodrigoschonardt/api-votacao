package rodrigoschonardt.votingapi.shared.exception;

public class InvalidSessionStateException extends RuntimeException {
    public InvalidSessionStateException(Long sessionId, String state) {
        super("Voting session with ID " + sessionId + " is " + state + " and cannot be updated.");
    }
}
