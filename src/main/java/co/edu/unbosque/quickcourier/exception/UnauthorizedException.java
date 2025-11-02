package co.edu.unbosque.quickcourier.exception;

/**
 * Excepción para acceso no autorizado (401)
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}