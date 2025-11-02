package co.edu.unbosque.quickcourier.model;


/**
 * Enum que representa el tipo de precio de un extra
 */
public enum PriceType {
    FIXED("Precio Fijo"),
    PERCENTAGE("Porcentaje");

    private final String displayName;

    PriceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}