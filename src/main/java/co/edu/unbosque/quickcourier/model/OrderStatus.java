package co.edu.unbosque.quickcourier.model;


/**
 * Enum que representa los estados de un pedido
 */
public enum OrderStatus {
    PENDING("Pendiente"),
    CONFIRMED("Confirmado"),
    IN_TRANSIT("En Tránsito"),
    DELIVERED("Entregado"),
    CANCELLED("Cancelado");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isFinalStatus() {
        return this == DELIVERED || this == CANCELLED;
    }

    public boolean canTransitionTo(OrderStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED -> newStatus == IN_TRANSIT || newStatus == CANCELLED;
            case IN_TRANSIT -> newStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
