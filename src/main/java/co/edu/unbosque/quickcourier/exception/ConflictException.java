package co.edu.unbosque.quickcourier.exception;

/**
 * Excepción para conflictos (409)
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}