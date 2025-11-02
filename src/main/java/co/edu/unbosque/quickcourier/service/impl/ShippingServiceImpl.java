package co.edu.unbosque.quickcourier.service.impl;

import co.edu.unbosque.quickcourier.dto.response.ShippingCalculationResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.ShippingExtraResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.ShippingRuleResponseDTO;
import co.edu.unbosque.quickcourier.exception.ResourceNotFoundException;
import co.edu.unbosque.quickcourier.mapper.DataMapper;
import co.edu.unbosque.quickcourier.model.Order;
import co.edu.unbosque.quickcourier.model.ShippingExtra;
import co.edu.unbosque.quickcourier.model.ShippingRule;
import co.edu.unbosque.quickcourier.repository.ShippingExtraRepository;
import co.edu.unbosque.quickcourier.repository.ShippingRuleRepository;
import co.edu.unbosque.quickcourier.service.ShippingService;
import co.edu.unbosque.quickcourier.strategy.ShippingStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de envío que utiliza el ShippingStrategyFactory
 * Demuestra cómo usar el patrón Strategy en la práctica
 */
@Service
@Transactional
public class ShippingServiceImpl implements ShippingService {

    private static final Logger logger = LoggerFactory.getLogger(ShippingServiceImpl.class);

    private final ShippingStrategyFactory shippingStrategyFactory;
    private final ShippingRuleRepository shippingRuleRepository;
    private final ShippingExtraRepository shippingExtraRepository;
    private final DataMapper dataMapper;

    public ShippingServiceImpl(ShippingStrategyFactory shippingStrategyFactory,
                               ShippingRuleRepository shippingRuleRepository,
                               ShippingExtraRepository shippingExtraRepository,
                               DataMapper dataMapper) {
        this.shippingStrategyFactory = shippingStrategyFactory;
        this.shippingRuleRepository = shippingRuleRepository;
        this.shippingExtraRepository = shippingExtraRepository;
        this.dataMapper = dataMapper;
    }

    /**
     * Calcula el costo de envío para un pedido
     * Utiliza el Factory para seleccionar automáticamente la mejor estrategia
     */
    @Override
    public ShippingCalculationResponseDTO calculateShippingCost(Order order) {
        logger.info("Calculating shipping cost for order: {}", order.getOrderNumber());

        // El Factory maneja toda la lógica de selección de estrategia
        var result = shippingStrategyFactory.calculateShipping(order);

        logger.info("Shipping calculation result for order {}: cost={}, rule={}",
                order.getOrderNumber(), result.shippingCost(), result.appliedRuleCode());

        // Convertir a DTO
        return dataMapper.toShippingCalculationResponseDTO(result);
    }

    /**
     * Calcula el costo forzando una regla específica
     * Útil para pruebas o casos especiales
     */
    @Override
    public ShippingCalculationResponseDTO calculateWithSpecificRule(Order order, String ruleCode) {
        logger.info("Calculating shipping with specific rule {} for order {}",
                ruleCode, order.getOrderNumber());

        var result = shippingStrategyFactory.calculateWithRule(order, ruleCode);

        return dataMapper.toShippingCalculationResponseDTO(result);
    }

    /**
     * Obtiene todas las reglas de envío activas
     * Cache de 5 minutos porque estas reglas cambian frecuentemente
     */
    @Override
    @Cacheable(value = "shippingRules", unless = "#result == null || #result.isEmpty()")
    public List<ShippingRuleResponseDTO> getActiveShippingRules() {
        logger.debug("Fetching active shipping rules");

        List<ShippingRule> rules = shippingStrategyFactory.getActiveRules();

        return rules.stream()
                .map(dataMapper::toShippingRuleResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una regla específica por código
     */
    @Override
    public ShippingRuleResponseDTO getShippingRuleByCode(String code) {
        logger.debug("Fetching shipping rule: {}", code);

        ShippingRule rule = shippingRuleRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Regla de envío no encontrada: " + code));

        return dataMapper.toShippingRuleResponseDTO(rule);
    }

    /**
     * Verifica si una regla específica es aplicable a un pedido
     * Útil para validaciones previas
     */
    @Override
    public boolean isRuleApplicable(Order order, String ruleCode) {
        try {
            shippingStrategyFactory.calculateWithRule(order, ruleCode);
            return true;
        } catch (IllegalStateException e) {
            logger.debug("Rule {} is not applicable: {}", ruleCode, e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene un extra de envío por código
     */
    @Override
    @Cacheable(value = "shippingExtras", key = "#code")
    public ShippingExtraResponseDTO getShippingExtraByCode(String code) {
        logger.debug("Fetching shipping extra: {}", code);

        ShippingExtra extra = shippingExtraRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Extra de envío no encontrado: " + code));

        return dataMapper.toShippingExtraResponseDTO(extra);
    }

    /**
     * Obtiene todos los extras de envío activos
     */
    @Override
    @Cacheable(value = "shippingExtras", key = "'all'")
    public List<ShippingExtraResponseDTO> getActiveShippingExtras() {
        logger.debug("Fetching all active shipping extras");

        List<ShippingExtra> extras = shippingExtraRepository.findAllActiveOrderedByDisplay();

        return extras.stream()
                .map(dataMapper::toShippingExtraResponseDTO)
                .collect(Collectors.toList());
    }
}