package co.edu.unbosque.quickcourier.exception;

/**
 * Excepción lanzada cuando un usuario excede el límite de requests permitidos
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}