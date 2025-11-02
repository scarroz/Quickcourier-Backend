package co.edu.unbosque.quickcourier.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponseDTO(
        Long id,
        String sku,
        String name,
        String description,
        CategoryResponseDTO category,
        BigDecimal price,
        BigDecimal weightKg,
        Integer stockQuantity,
        Boolean isActive,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
