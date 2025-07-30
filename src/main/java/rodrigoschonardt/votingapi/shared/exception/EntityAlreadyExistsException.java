package rodrigoschonardt.votingapi.shared.exception;

public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String entityType, String identifier) {
        super(entityType + " with " + identifier + " already exists");
    }
}
