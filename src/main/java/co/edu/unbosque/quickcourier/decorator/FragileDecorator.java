package co.edu.unbosque.quickcourier.decorator;

import co.edu.unbosque.quickcourier.model.ShippingExtra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorador Concreto para Manejo Frágil
 * Agrega cuidado especial para productos frágiles
 *
 * Extra Code: FRAGILE
 * Tipo: FIXED
 * Precio base: $5,000
 */
public class FragileDecorator extends OrderDecorator {

    private static final Logger logger = LoggerFactory.getLogger(FragileDecorator.class);

    public FragileDecorator(OrderComponent wrappedOrder, ShippingExtra shippingExtra) {
        super(wrappedOrder, shippingExtra);

        if (!"FRAGILE".equals(shippingExtra.getCode())) {
            logger.warn("Fragile decorator created with non-FRAGILE extra: {}",
                    shippingExtra.getCode());
        }

        logger.debug("Fragile handling added to order. Cost: ${}", getExtraCost());
    }

    @Override
    protected String getExtraDescription() {
        return String.format("Manejo Frágil (cuidado especial) +$%s", getExtraCost());
    }

    /**
     * Obtiene instrucciones especiales para el conductor
     */
    public String getHandlingInstructions() {
        return "FRÁGIL: Manejar con extremo cuidado. " +
                "No apilar. Mantener en posición vertical. " +
                "Evitar movimientos bruscos durante el transporte.";
    }

    /**
     * Verifica si el embalaje es apropiado para productos frágiles
     */
    public boolean requiresSpecialPackaging() {
        return true;
    }
}