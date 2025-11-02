package co.edu.unbosque.quickcourier.decorator;

import co.edu.unbosque.quickcourier.model.ShippingExtra;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Decorador Abstracto (Abstract Decorator)
 * Base para todos los decoradores de extras
 */
public abstract class OrderDecorator implements OrderComponent {

    protected final OrderComponent wrappedOrder;
    protected final ShippingExtra shippingExtra;

    protected OrderDecorator(OrderComponent wrappedOrder, ShippingExtra shippingExtra) {
        if (wrappedOrder == null) {
            throw new IllegalArgumentException("Wrapped order cannot be null");
        }
        if (shippingExtra == null) {
            throw new IllegalArgumentException("Shipping extra cannot be null");
        }
        if (!shippingExtra.getIsActive()) {
            throw new IllegalStateException(
                    "Cannot apply inactive shipping extra: " + shippingExtra.getCode());
        }

        this.wrappedOrder = wrappedOrder;
        this.shippingExtra = shippingExtra;
    }

    @Override
    public BigDecimal getCost() {
        // Costo base + costo del extra
        return wrappedOrder.getCost().add(getExtraCost());
    }

    @Override
    public String getDescription() {
        // Descripción del wrapped order + descripción del extra
        return wrappedOrder.getDescription() + " + " + getExtraDescription();
    }

    @Override
    public List<String> getAppliedExtras() {
        // Lista de extras del wrapped order + este extra
        List<String> extras = new ArrayList<>(wrappedOrder.getAppliedExtras());
        extras.add(shippingExtra.getCode());
        return extras;
    }

    @Override
    public BigDecimal getWeight() {
        // Por defecto, los extras no afectan el peso
        return wrappedOrder.getWeight();
    }

    /**
     * Calcula el costo específico de este extra
     * Puede ser fijo o porcentual según el tipo
     *
     * @return Costo del extra
     */
    protected BigDecimal getExtraCost() {
        // Obtener el subtotal del pedido base para cálculo porcentual
        BigDecimal baseSubtotal = getBaseSubtotal();
        return shippingExtra.calculatePrice(baseSubtotal);
    }

    /**
     * Obtiene la descripción específica de este extra
     *
     * @return Descripción del extra
     */
    protected abstract String getExtraDescription();

    /**
     * Obtiene el subtotal base del pedido (sin extras)
     * Útil para cálculos porcentuales
     */
    private BigDecimal getBaseSubtotal() {
        OrderComponent current = wrappedOrder;

        // Desenrollar hasta encontrar BaseOrder
        while (current instanceof OrderDecorator) {
            current = ((OrderDecorator) current).wrappedOrder;
        }

        if (current instanceof BaseOrder) {
            return ((BaseOrder) current).getOrder().getSubtotal();
        }

        return BigDecimal.ZERO;
    }

    /**
     * Obtiene el ShippingExtra asociado
     */
    public ShippingExtra getShippingExtra() {
        return shippingExtra;
    }
}