package co.edu.unbosque.quickcourier.decorator;

import co.edu.unbosque.quickcourier.model.ShippingExtra;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Decorador Concreto para Seguro de Envío
 * Agrega seguro contra pérdidas y daños
 *
 * Extra Code: INSURANCE
 * Tipo: PERCENTAGE
 * Porcentaje: 5% del subtotal
 */
public class InsuranceDecorator extends OrderDecorator {

    private static final Logger logger = LoggerFactory.getLogger(InsuranceDecorator.class);

    public InsuranceDecorator(OrderComponent wrappedOrder, ShippingExtra shippingExtra) {
        super(wrappedOrder, shippingExtra);

        if (!"INSURANCE".equals(shippingExtra.getCode())) {
            logger.warn("Insurance decorator created with non-INSURANCE extra: {}",
                    shippingExtra.getCode());
        }

        logger.debug("Insurance coverage added to order. Cost: ${}", getExtraCost());
    }

    @Override
    protected String getExtraDescription() {
        BigDecimal percentage = shippingExtra.getPercentageValue() != null
                ? shippingExtra.getPercentageValue()
                : BigDecimal.valueOf(5.0);

        return String.format("Seguro (%.0f%% del subtotal) +$%s",
                percentage, getExtraCost());
    }

    /**
     * Obtiene el valor máximo cubierto por el seguro
     */
    public BigDecimal getCoverageAmount() {
        // El seguro cubre el subtotal completo del pedido
        OrderComponent current = wrappedOrder;

        while (current instanceof OrderDecorator) {
            current = ((OrderDecorator) current).wrappedOrder;
        }

        if (current instanceof BaseOrder) {
            return ((BaseOrder) current).getOrder().getSubtotal();
        }

        return BigDecimal.ZERO;
    }

    /**
     * Obtiene las condiciones del seguro
     */
    public String getInsuranceTerms() {
        return "Cobertura total contra pérdida o daño durante el transporte. " +
                "Reembolso del 100% del valor declarado en caso de siniestro. " +
                "Válido por 30 días desde la fecha de envío.";
    }

    /**
     * Verifica si el monto del pedido requiere seguro obligatorio
     */
    public static boolean isInsuranceRequired(BigDecimal orderValue) {
        // Seguro obligatorio para pedidos mayores a $500,000
        BigDecimal threshold = new BigDecimal("500000.00");
        return orderValue.compareTo(threshold) > 0;
    }
}