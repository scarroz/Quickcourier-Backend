package co.edu.unbosque.quickcourier.dto.response;

import java.math.BigDecimal;

public record ShippingCalculationResponseDTO(
        BigDecimal shippingCost,
        String appliedRuleCode,
        String appliedRuleName,
        String calculationDetails
) {}
