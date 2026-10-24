package vn.ktt.eartraining.application.access;

/** Also raised for resources owned by someone else, so their existence is not leaked. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
