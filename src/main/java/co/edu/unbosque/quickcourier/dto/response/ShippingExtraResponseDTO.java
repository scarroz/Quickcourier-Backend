package co.edu.unbosque.quickcourier.dto.response;

import java.math.BigDecimal;

public record ShippingExtraResponseDTO(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal basePrice,
        String priceType,
        BigDecimal percentageValue,
        Boolean isActive,
        Integer displayOrder
) {}
