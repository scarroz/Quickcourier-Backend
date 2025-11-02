package co.edu.unbosque.quickcourier.strategy;


import co.edu.unbosque.quickcourier.model.Order;
import co.edu.unbosque.quickcourier.model.ShippingRule;
import co.edu.unbosque.quickcourier.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Estrategia de envío gratis para primera compra
 * Si es el primer pedido del usuario, el envío es gratuito
 *
 * Configuración esperada en ShippingRule:
 * {
 *   "conditions": {
 *     "is_first_order": true
 *   }
 * }
 */
@Component
public class FirstOrderStrategy implements ShippingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(FirstOrderStrategy.class);
    private static final String STRATEGY_TYPE = "FIRST_ORDER";

    private final OrderRepository orderRepository;

    public FirstOrderStrategy(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public BigDecimal calculateShippingCost(Order order, ShippingRule rule) {
        // Primera compra = Envío gratis
        logger.info("First order shipping cost: FREE for user {}", order.getUser().getEmail());
        return BigDecimal.ZERO;
    }

    @Override
    public boolean isApplicable(Order order, ShippingRule rule) {
        // Verificar que la regla esté activa
        if (!rule.isCurrentlyValid()) {
            logger.debug("Rule {} is not currently valid", rule.getCode());
            return false;
        }

        // Verificar condición de configuración
        @SuppressWarnings("unchecked")
        Map<String, Object> conditions =
                (Map<String, Object>) rule.getConfiguration().get("conditions");

        if (conditions == null) {
            logger.warn("No conditions found in rule configuration");
            return false;
        }

        Boolean isFirstOrderRequired = (Boolean) conditions.get("is_first_order");
        if (isFirstOrderRequired == null || !isFirstOrderRequired) {
            logger.debug("First order condition not enabled");
            return false;
        }

        // Verificar si es el primer pedido del usuario
        Long userId = order.getUser().getId();
        long orderCount = orderRepository.countByUserId(userId);

        // Es el primer pedido si el conteo es 0 (este pedido aún no se ha guardado)
        boolean isFirstOrder = orderCount == 0;

        if (isFirstOrder) {
            logger.info("First order detected for user {} - Free shipping applies",
                    order.getUser().getEmail());
        } else {
            logger.debug("User {} already has {} orders",
                    order.getUser().getEmail(), orderCount);
        }

        return isFirstOrder;
    }

    @Override
    public String getStrategyType() {
        return STRATEGY_TYPE;
    }

    @Override
    public String getCalculationDescription(Order order, ShippingRule rule) {
        return "¡Envío GRATIS por primera compra! Bienvenido a QuickCourier";
    }
}