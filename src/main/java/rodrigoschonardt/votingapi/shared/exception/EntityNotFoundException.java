package rodrigoschonardt.votingapi.shared.exception;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String entityType, String identifier) {
        super(entityType + " with " + identifier + " does not exist");
    }
}
