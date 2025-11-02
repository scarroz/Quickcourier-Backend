package co.edu.unbosque.quickcourier.dto.response;

import java.math.BigDecimal;

public record ProductSummaryResponseDTO(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        Integer stockQuantity,
        Boolean isActive,
        String imageUrl
) {}