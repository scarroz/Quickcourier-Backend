package co.edu.unbosque.quickcourier.exception;

/**
 * Excepción para operaciones prohibidas (403)
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}