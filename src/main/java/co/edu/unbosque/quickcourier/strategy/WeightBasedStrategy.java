package co.edu.unbosque.quickcourier.strategy;


import co.edu.unbosque.quickcourier.model.Order;
import co.edu.unbosque.quickcourier.model.ShippingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Estrategia de costo basado en peso
 * Calcula el costo según el peso total del pedido
 * Incluye umbral de envío gratis si se supera cierto peso
 *
 * Configuración esperada en ShippingRule:
 * {
 *   "base_rate": 5000,
 *   "rate_per_kg": 2000,
 *   "free_shipping_threshold_kg": 10
 * }
 */
@Component
public class WeightBasedStrategy implements ShippingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(WeightBasedStrategy.class);
    private static final String STRATEGY_TYPE = "WEIGHT_BASED";
    private static final BigDecimal DEFAULT_BASE_RATE = new BigDecimal("5000.00");
    private static final BigDecimal DEFAULT_RATE_PER_KG = new BigDecimal("2000.00");
    private static final BigDecimal DEFAULT_FREE_THRESHOLD = new BigDecimal("10.0");

    @Override
    public BigDecimal calculateShippingCost(Order order, ShippingRule rule) {
        BigDecimal totalWeight = order.getTotalWeightKg();

        // Obtener configuración
        Double baseRate = rule.getConfigValue("base_rate", Double.class);
        Double ratePerKg = rule.getConfigValue("rate_per_kg", Double.class);
        Double freeShippingThreshold = rule.getConfigValue("free_shipping_threshold_kg", Double.class);

        // Aplicar defaults si no están configurados
        BigDecimal baseRateValue = baseRate != null
                ? BigDecimal.valueOf(baseRate)
                : DEFAULT_BASE_RATE;

        BigDecimal ratePerKgValue = ratePerKg != null
                ? BigDecimal.valueOf(ratePerKg)
                : DEFAULT_RATE_PER_KG;

        BigDecimal freeShippingThresholdValue = freeShippingThreshold != null
                ? BigDecimal.valueOf(freeShippingThreshold)
                : DEFAULT_FREE_THRESHOLD;

        // Si supera el umbral, envío gratis
        if (totalWeight.compareTo(freeShippingThresholdValue) >= 0) {
            logger.info("Weight {} kg exceeds free shipping threshold {} kg - FREE SHIPPING",
                    totalWeight, freeShippingThresholdValue);
            return BigDecimal.ZERO;
        }

        // Calcular: base_rate + (peso * rate_per_kg)
        BigDecimal weightCost = totalWeight.multiply(ratePerKgValue);
        BigDecimal totalCost = baseRateValue.add(weightCost);

        logger.debug("Weight-based calculation: base={}, weight={}kg, rate_per_kg={}, total={}",
                baseRateValue, totalWeight, ratePerKgValue, totalCost);

        return totalCost.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean isApplicable(Order order, ShippingRule rule) {
        // Verificar que la regla esté activa
        if (!rule.isCurrentlyValid()) {
            logger.debug("Rule {} is not currently valid", rule.getCode());
            return false;
        }

        // Esta estrategia siempre es aplicable si está activa
        // Es la estrategia "por defecto" con menor prioridad
        logger.debug("Weight-based strategy is applicable (default fallback)");
        return true;
    }

    @Override
    public String getStrategyType() {
        return STRATEGY_TYPE;
    }

    @Override
    public String getCalculationDescription(Order order, ShippingRule rule) {
        BigDecimal totalWeight = order.getTotalWeightKg();
        Double freeShippingThreshold = rule.getConfigValue("free_shipping_threshold_kg", Double.class);

        if (freeShippingThreshold != null &&
                totalWeight.compareTo(BigDecimal.valueOf(freeShippingThreshold)) >= 0) {
            return String.format("¡Envío GRATIS! Peso supera %.2f kg", freeShippingThreshold);
        }

        Double baseRate = rule.getConfigValue("base_rate", Double.class);
        Double ratePerKg = rule.getConfigValue("rate_per_kg", Double.class);

        return String.format("Cálculo por peso: Base $%.0f + $%.0f por kg (Total: %.2f kg)",
                baseRate != null ? baseRate : DEFAULT_BASE_RATE.doubleValue(),
                ratePerKg != null ? ratePerKg : DEFAULT_RATE_PER_KG.doubleValue(),
                totalWeight.doubleValue());
    }
}