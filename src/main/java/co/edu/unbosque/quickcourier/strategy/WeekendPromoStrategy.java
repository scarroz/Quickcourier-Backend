package co.edu.unbosque.quickcourier.strategy;

import co.edu.unbosque.quickcourier.model.Order;
import co.edu.unbosque.quickcourier.model.ShippingRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Estrategia de descuento para fines de semana
 * Aplica un porcentaje de descuento si el pedido se realiza en días específicos
 *
 * Configuración esperada en ShippingRule:
 * {
 *   "discount_percentage": 20,
 *   "applicable_days": ["SATURDAY", "SUNDAY"]
 * }
 */
@Component
public class WeekendPromoStrategy implements ShippingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(WeekendPromoStrategy.class);
    private static final String STRATEGY_TYPE = "WEEKEND_PROMO";
    private static final BigDecimal BASE_SHIPPING_COST = new BigDecimal("10000.00");
    private static final BigDecimal RATE_PER_KG = new BigDecimal("2000.00");

    @Override
    public BigDecimal calculateShippingCost(Order order, ShippingRule rule) {
        // Obtener descuento de la configuración
        Double discountPercentage = rule.getConfigValue("discount_percentage", Double.class);
        if (discountPercentage == null) {
            discountPercentage = 20.0; // Default 20%
        }

        // Calcular costo base según peso
        BigDecimal baseCost = calculateBaseCostByWeight(order);

        // Aplicar descuento
        BigDecimal discount = baseCost.multiply(BigDecimal.valueOf(discountPercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal finalCost = baseCost.subtract(discount);

        logger.debug("Weekend promo applied: base={}, discount={}%, final={}",
                baseCost, discountPercentage, finalCost);

        return finalCost.max(BigDecimal.ZERO);
    }

    @Override
    public boolean isApplicable(Order order, ShippingRule rule) {
        // Verificar que la regla esté activa
        if (!rule.isCurrentlyValid()) {
            logger.debug("Rule {} is not currently valid", rule.getCode());
            return false;
        }

        // Obtener días aplicables de la configuración
        @SuppressWarnings("unchecked")
        List<String> applicableDays = (List<String>) rule.getConfiguration().get("applicable_days");

        if (applicableDays == null || applicableDays.isEmpty()) {
            applicableDays = List.of("SATURDAY", "SUNDAY");
        }

        // Verificar si hoy es un día aplicable
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek currentDay = now.getDayOfWeek();

        boolean isApplicable = applicableDays.stream()
                .anyMatch(day -> day.equalsIgnoreCase(currentDay.name()));

        if (isApplicable) {
            logger.debug("Weekend promo is applicable for day: {}", currentDay);
        } else {
            logger.debug("Weekend promo not applicable for day: {}", currentDay);
        }

        return isApplicable;
    }

    @Override
    public String getStrategyType() {
        return STRATEGY_TYPE;
    }

    @Override
    public String getCalculationDescription(Order order, ShippingRule rule) {
        Double discountPercentage = rule.getConfigValue("discount_percentage", Double.class);
        return String.format("Promo fin de semana - Descuento del %d%% aplicado",
                discountPercentage != null ? discountPercentage.intValue() : 20);
    }

    /**
     * Calcula costo base según peso del pedido
     */
    private BigDecimal calculateBaseCostByWeight(Order order) {
        BigDecimal weight = order.getTotalWeightKg();
        return BASE_SHIPPING_COST.add(weight.multiply(RATE_PER_KG));
    }
}