package co.edu.unbosque.quickcourier.decorator;

import co.edu.unbosque.quickcourier.model.ShippingExtra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Decorador Concreto para Empaque de Regalo
 * Agrega envoltorio especial y tarjeta de regalo
 *
 * Extra Code: GIFT_WRAP
 * Tipo: FIXED
 * Precio base: $8,000
 */
public class GiftWrapDecorator extends OrderDecorator {

    private static final Logger logger = LoggerFactory.getLogger(GiftWrapDecorator.class);
    private static final BigDecimal WEIGHT_ADDITION = new BigDecimal("0.2"); // 200g adicionales

    private String giftMessage;

    public GiftWrapDecorator(OrderComponent wrappedOrder, ShippingExtra shippingExtra) {
        super(wrappedOrder, shippingExtra);

        if (!"GIFT_WRAP".equals(shippingExtra.getCode())) {
            logger.warn("Gift wrap decorator created with non-GIFT_WRAP extra: {}",
                    shippingExtra.getCode());
        }

        logger.debug("Gift wrapping added to order. Cost: ${}", getExtraCost());
    }

    @Override
    protected String getExtraDescription() {
        return String.format("Empaque de Regalo (envoltorio especial + tarjeta) +$%s",
                getExtraCost());
    }

    @Override
    public BigDecimal getWeight() {
        // El empaque de regalo agrega peso adicional
        return wrappedOrder.getWeight().add(WEIGHT_ADDITION);
    }

    /**
     * Establece el mensaje de la tarjeta de regalo
     */
    public void setGiftMessage(String message) {
        if (message != null && message.length() > 200) {
            throw new IllegalArgumentException(
                    "Gift message cannot exceed 200 characters");
        }
        this.giftMessage = message;
        logger.debug("Gift message set: {}", message != null ? "Yes" : "No");
    }

    /**
     * Obtiene el mensaje de la tarjeta
     */
    public String getGiftMessage() {
        return giftMessage;
    }

    /**
     * Obtiene las opciones de papel de regalo disponibles
     */
    public String[] getAvailableWrapOptions() {
        return new String[]{
                "Papel elegante dorado",
                "Papel plateado con moño",
                "Papel de colores festivos",
                "Papel kraft con cinta natural",
                "Papel temático (cumpleaños, navidad, etc.)"
        };
    }

    /**
     * Verifica si incluye tarjeta de regalo
     */
    public boolean includesGiftCard() {
        return true;
    }
}