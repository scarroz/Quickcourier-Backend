package co.edu.unbosque.quickcourier.strategy;


import co.edu.unbosque.quickcourier.model.Order;
import co.edu.unbosque.quickcourier.model.ShippingRule;

import java.math.BigDecimal;

/**
 * Interface Strategy para cálculo de costo de envío
 * Permite implementar diferentes algoritmos de cálculo de manera intercambiable
 */
public interface ShippingStrategy {

    /**
     * Calcula el costo de envío para un pedido
     *
     * @param order Pedido para calcular el envío
     * @param rule Regla de envío con configuración específica
     * @return Costo de envío calculado
     */
    BigDecimal calculateShippingCost(Order order, ShippingRule rule);

    /**
     * Verifica si esta estrategia es aplicable al pedido
     *
     * @param order Pedido a verificar
     * @param rule Regla de envío
     * @return true si la estrategia puede aplicarse
     */
    boolean isApplicable(Order order, ShippingRule rule);

    /**
     * Retorna el tipo de estrategia que maneja
     * Debe coincidir con el campo rule_type en la base de datos
     *
     * @return Tipo de estrategia (WEEKEND_PROMO, FIRST_ORDER, etc)
     */
    String getStrategyType();

    /**
     * Proporciona descripción del cálculo realizado
     *
     * @param order Pedido
     * @param rule Regla aplicada
     * @return Descripción del cálculo
     */
    default String getCalculationDescription(Order order, ShippingRule rule) {
        return "Cálculo de envío aplicando: " + rule.getName();
    }
}