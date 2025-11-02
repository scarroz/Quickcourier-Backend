package co.edu.unbosque.quickcourier.decorator;

import co.edu.unbosque.quickcourier.model.ShippingExtra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorador Concreto para Entrega Exprés
 * Agrega servicio de entrega rápida (menos de 2 horas)
 *
 * Extra Code: EXPRESS
 * Tipo: FIXED
 * Precio base: $15,000
 */
public class ExpressDecorator extends OrderDecorator {

    private static final Logger logger = LoggerFactory.getLogger(ExpressDecorator.class);

    public ExpressDecorator(OrderComponent wrappedOrder, ShippingExtra shippingExtra) {
        super(wrappedOrder, shippingExtra);

        if (!"EXPRESS".equals(shippingExtra.getCode())) {
            logger.warn("Express decorator created with non-EXPRESS extra: {}",
                    shippingExtra.getCode());
        }

        logger.debug("Express delivery added to order. Cost: ${}", getExtraCost());
    }

    @Override
    protected String getExtraDescription() {
        return String.format("Entrega Exprés (< 2 horas) +$%s", getExtraCost());
    }

    /**
     * Validaciones específicas para entrega exprés
     * Por ejemplo, verificar horarios de disponibilidad
     */
    public boolean isAvailableForTimeSlot(java.time.LocalDateTime deliveryTime) {
        // Entrega exprés solo disponible en horario laboral (8:00 - 18:00)
        int hour = deliveryTime.getHour();
        boolean available = hour >= 8 && hour <= 18;

        if (!available) {
            logger.warn("Express delivery not available for time slot: {}", deliveryTime);
        }

        return available;
    }
}