package co.edu.unbosque.quickcourier.exception;

/**
 * Excepción para errores internos del servidor (500)
 */
public class InternalServerException extends RuntimeException {
    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}