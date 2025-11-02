package co.edu.unbosque.quickcourier.model;


/**
 * Enum que representa los estados de pago de un pedido
 */
public enum PaymentStatus {
    PENDING("Pendiente"),
    PAID("Pagado"),
    FAILED("Fallido"),
    REFUNDED("Reembolsado");

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSuccessful() {
        return this == PAID;
    }

    public boolean canBeRefunded() {
        return this == PAID;
    }
}