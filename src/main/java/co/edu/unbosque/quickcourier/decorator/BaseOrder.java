package co.edu.unbosque.quickcourier.decorator;

import co.edu.unbosque.quickcourier.model.Order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente Concreto (Concrete Component)
 * Representa un pedido base sin extras
 */
public class BaseOrder implements OrderComponent {

    private final Order order;

    public BaseOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        this.order = order;
    }

    @Override
    public BigDecimal getCost() {
        // Costo base: subtotal + costo de envío
        return order.getSubtotal().add(order.getShippingCost());
    }

    @Override
    public String getDescription() {
        return String.format("Pedido base: %s (Subtotal: $%s, Envío: $%s)",
                order.getOrderNumber(),
                order.getSubtotal(),
                order.getShippingCost());
    }

    @Override
    public List<String> getAppliedExtras() {
        // Pedido base no tiene extras
        return new ArrayList<>();
    }

    @Override
    public BigDecimal getWeight() {
        return order.getTotalWeightKg();
    }

    /**
     * Obtiene la orden subyacente
     * Útil para acceder a propiedades específicas
     */
    public Order getOrder() {
        return order;
    }
}