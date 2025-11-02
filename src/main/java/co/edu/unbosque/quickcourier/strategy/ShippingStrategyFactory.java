package co.edu.unbosque.quickcourier.strategy;


import co.edu.unbosque.quickcourier.model.Order;
import co.edu.unbosque.quickcourier.model.ShippingRule;
import co.edu.unbosque.quickcourier.repository.ShippingRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory para seleccionar y aplicar la estrategia de envío apropiada
 * Implementa el patrón Strategy con selección dinámica basada en reglas de negocio
 *
 * Flujo de selección:
 * 1. Obtiene reglas activas ordenadas por prioridad
 * 2. Para cada regla, obtiene la estrategia correspondiente
 * 3. Verifica si la estrategia es aplicable al pedido
 * 4. Aplica la primera estrategia que cumpla las condiciones
 * 5. Si ninguna aplica, usa costo por defecto
 */
@Component
public class ShippingStrategyFactory {

    private static final Logger logger = LoggerFactory.getLogger(ShippingStrategyFactory.class);
    private static final BigDecimal DEFAULT_SHIPPING_COST = new BigDecimal("10000.00");

    private final Map<String, ShippingStrategy> strategies;
    private final ShippingRuleRepository shippingRuleRepository;

    /**
     * Constructor que auto-registra todas las estrategias disponibles
     * Spring inyecta automáticamente todas las implementaciones de ShippingStrategy
     */
    public ShippingStrategyFactory(List<ShippingStrategy> strategyList,
                                   ShippingRuleRepository shippingRuleRepository) {
        // Crear mapa de estrategias indexado por tipo
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        ShippingStrategy::getStrategyType,
                        Function.identity()
                ));

        this.shippingRuleRepository = shippingRuleRepository;

        logger.info("ShippingStrategyFactory initialized with {} strategies: {}",
                strategies.size(), strategies.keySet());
    }

    /**
     * Calcula el costo de envío seleccionando automáticamente la estrategia más apropiada
     *
     * @param order Pedido a calcular
     * @return Resultado del cálculo con detalles
     */
    public ShippingCalculationResult calculateShipping(Order order) {
        logger.debug("Calculating shipping cost for order: {}", order.getOrderNumber());

        // Obtener reglas activas ordenadas por prioridad (menor = mayor prioridad)
        List<ShippingRule> activeRules = shippingRuleRepository
                .findActiveAndValidRules(LocalDateTime.now());

        logger.debug("Found {} active shipping rules for order {}",
                activeRules.size(), order.getOrderNumber());

        // Iterar por prioridad y aplicar la primera regla aplicable
        for (ShippingRule rule : activeRules) {
            ShippingStrategy strategy = strategies.get(rule.getRuleType());

            if (strategy == null) {
                logger.warn("No strategy found for rule type: {} (rule: {})",
                        rule.getRuleType(), rule.getCode());
                continue;
            }

            logger.debug("Checking strategy {} with rule {}",
                    strategy.getStrategyType(), rule.getCode());

            if (strategy.isApplicable(order, rule)) {
                BigDecimal shippingCost = strategy.calculateShippingCost(order, rule);
                String description = strategy.getCalculationDescription(order, rule);

                logger.info("✓ Applied shipping rule '{}' (type: {}) to order {}. Cost: {}",
                        rule.getCode(), rule.getRuleType(), order.getOrderNumber(), shippingCost);

                return new ShippingCalculationResult(
                        shippingCost,
                        rule.getCode(),
                        rule.getName(),
                        description,
                        true
                );
            }
        }

        // Si ninguna regla aplica, usar costo por defecto
        logger.warn("✗ No applicable shipping rule found for order {}. Using default cost: {}",
                order.getOrderNumber(), DEFAULT_SHIPPING_COST);

        return getDefaultShippingCost(order);
    }

    /**
     * Calcula costo de envío usando una regla específica
     * Útil cuando el usuario o el sistema necesita forzar una regla particular
     *
     * @param order Pedido
     * @param ruleCode Código de la regla a aplicar
     * @return Resultado del cálculo
     * @throws IllegalArgumentException si la regla no existe
     * @throws IllegalStateException si la regla no está activa o no es aplicable
     */
    public ShippingCalculationResult calculateWithRule(Order order, String ruleCode) {
        logger.debug("Calculating shipping with specific rule: {}", ruleCode);

        Optional<ShippingRule> ruleOpt = shippingRuleRepository.findByCode(ruleCode);

        if (ruleOpt.isEmpty()) {
            logger.error("Shipping rule not found: {}", ruleCode);
            throw new IllegalArgumentException("Regla de envío no encontrada: " + ruleCode);
        }

        ShippingRule rule = ruleOpt.get();

        if (!rule.isCurrentlyValid()) {
            logger.error("Shipping rule is not active or valid: {}", ruleCode);
            throw new IllegalStateException("Regla de envío no está activa: " + ruleCode);
        }

        ShippingStrategy strategy = strategies.get(rule.getRuleType());

        if (strategy == null) {
            logger.error("No strategy found for rule type: {} (rule: {})",
                    rule.getRuleType(), ruleCode);
            throw new IllegalStateException(
                    "Estrategia no encontrada para tipo: " + rule.getRuleType());
        }

        if (!strategy.isApplicable(order, rule)) {
            logger.warn("Strategy {} is not applicable for order {} with rule {}",
                    strategy.getStrategyType(), order.getOrderNumber(), ruleCode);
            throw new IllegalStateException("La regla no es aplicable a este pedido");
        }

        BigDecimal shippingCost = strategy.calculateShippingCost(order, rule);
        String description = strategy.getCalculationDescription(order, rule);

        logger.info("Applied forced rule '{}' to order {}. Cost: {}",
                ruleCode, order.getOrderNumber(), shippingCost);

        return new ShippingCalculationResult(
                shippingCost,
                rule.getCode(),
                rule.getName(),
                description,
                true
        );
    }

    /**
     * Obtiene todas las estrategias disponibles
     * Útil para debugging o monitoring
     */
    public Map<String, ShippingStrategy> getAvailableStrategies() {
        return Map.copyOf(strategies);
    }

    /**
     * Obtiene todas las reglas activas
     */
    public List<ShippingRule> getActiveRules() {
        return shippingRuleRepository.findActiveAndValidRules(LocalDateTime.now());
    }

    /**
     * Costo de envío por defecto si ninguna regla aplica
     */
    private ShippingCalculationResult getDefaultShippingCost(Order order) {
        return new ShippingCalculationResult(
                DEFAULT_SHIPPING_COST,
                "DEFAULT",
                "Tarifa estándar",
                "Costo de envío estándar - Ninguna regla especial aplicable",
                false
        );
    }

    /**
     * Record para encapsular el resultado del cálculo de envío
     * Proporciona toda la información necesaria para el pedido y para auditoría
     */
    public record ShippingCalculationResult(
            BigDecimal shippingCost,
            String appliedRuleCode,
            String appliedRuleName,
            String calculationDetails,
            boolean ruleApplied
    ) {
        public ShippingCalculationResult {
            if (shippingCost == null) {
                throw new IllegalArgumentException("Shipping cost cannot be null");
            }
            if (shippingCost.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Shipping cost cannot be negative");
            }
        }
    }
}