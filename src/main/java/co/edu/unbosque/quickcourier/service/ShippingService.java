package co.edu.unbosque.quickcourier.service;

import co.edu.unbosque.quickcourier.dto.response.ShippingCalculationResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.ShippingExtraResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.ShippingRuleResponseDTO;
import co.edu.unbosque.quickcourier.model.Order;

import java.util.List;

/**
 * Servicio para gestión de envíos y cálculo de costos
 */
public interface ShippingService {

    /**
     * Calcula el costo de envío para un pedido usando la mejor estrategia disponible
     */
    ShippingCalculationResponseDTO calculateShippingCost(Order order);

    /**
     * Calcula el costo de envío usando una regla específica
     */
    ShippingCalculationResponseDTO calculateWithSpecificRule(Order order, String ruleCode);

    /**
     * Obtiene todas las reglas de envío activas
     */
    List<ShippingRuleResponseDTO> getActiveShippingRules();

    /**
     * Obtiene una regla de envío por código
     */
    ShippingRuleResponseDTO getShippingRuleByCode(String code);

    /**
     * Verifica si una regla es aplicable a un pedido
     */
    boolean isRuleApplicable(Order order, String ruleCode);

    ShippingExtraResponseDTO getShippingExtraByCode(String code);
    List<ShippingExtraResponseDTO> getActiveShippingExtras();


}