package co.edu.unbosque.quickcourier.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ShippingRuleResponseDTO(
        Long id,
        String code,
        String name,
        String description,
        String ruleType,
        Integer priority,
        Boolean isActive,
        Map<String, Object> configuration,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        LocalDateTime createdAt
) {}
