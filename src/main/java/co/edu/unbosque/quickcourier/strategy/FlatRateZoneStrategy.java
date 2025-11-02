package co.edu.unbosque.quickcourier.strategy;


import co.edu.unbosque.quickcourier.model.Order;
import co.edu.unbosque.quickcourier.model.ShippingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estrategia de tarifa plana por zona
 * Aplica un costo fijo según la zona de entrega
 *
 * Configuración esperada en ShippingRule:
 * {
 *   "zone": "Norte",
 *   "flat_rate": 8000
 * }
 */
@Component
public class FlatRateZoneStrategy implements ShippingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(FlatRateZoneStrategy.class);
    private static final String STRATEGY_TYPE = "FLAT_RATE_ZONE";
    private static final BigDecimal DEFAULT_FLAT_RATE = new BigDecimal("8000.00");

    @Override
    public BigDecimal calculateShippingCost(Order order, ShippingRule rule) {
        // Obtener tarifa plana de la configuración
        Object flatRateObj = rule.getConfiguration().get("flat_rate");

        if (flatRateObj == null) {
            logger.debug("No flat_rate configured, using default: {}", DEFAULT_FLAT_RATE);
            return DEFAULT_FLAT_RATE;
        }

        // Convertir a BigDecimal
        BigDecimal flatRate;
        if (flatRateObj instanceof Number) {
            flatRate = BigDecimal.valueOf(((Number) flatRateObj).doubleValue());
        } else {
            flatRate = new BigDecimal(flatRateObj.toString());
        }

        logger.debug("Flat rate for zone {}: {}", order.getAddress().getZone(), flatRate);
        return flatRate;
    }

    @Override
    public boolean isApplicable(Order order, ShippingRule rule) {
        // Verificar que la regla esté activa
        if (!rule.isCurrentlyValid()) {
            logger.debug("Rule {} is not currently valid", rule.getCode());
            return false;
        }

        // Obtener zona configurada en la regla
        String configuredZone = rule.getConfigValue("zone", String.class);

        if (configuredZone == null || configuredZone.isBlank()) {
            logger.warn("No zone configured in rule {}", rule.getCode());
            return false;
        }

        // Obtener zona del pedido
        String orderZone = order.getAddress().getZone();

        // Verificar coincidencia de zona (case-insensitive)
        boolean matches = configuredZone.equalsIgnoreCase(orderZone);

        if (matches) {
            logger.debug("Flat rate zone matches: configured={}, order={}",
                    configuredZone, orderZone);
        } else {
            logger.debug("Flat rate zone does not match: configured={}, order={}",
                    configuredZone, orderZone);
        }

        return matches;
    }

    @Override
    public String getStrategyType() {
        return STRATEGY_TYPE;
    }

    @Override
    public String getCalculationDescription(Order order, ShippingRule rule) {
        String zone = rule.getConfigValue("zone", String.class);
        Object flatRate = rule.getConfiguration().get("flat_rate");

        return String.format("Tarifa plana para zona %s: $%s",
                zone != null ? zone : "N/A",
                flatRate != null ? flatRate.toString() : "0");
    }
}