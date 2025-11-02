package co.edu.unbosque.quickcourier.exception;

/**
 * Excepción para peticiones inválidas (400)
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}