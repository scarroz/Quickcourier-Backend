package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateProductRequestDTO(
        @Size(max = 255)
        String name,

        String description,

        Long categoryId,

        @DecimalMin(value = "0.0")
        BigDecimal price,

        @DecimalMin(value = "0.001")
        BigDecimal weightKg,

        @Min(value = 0)
        Integer stockQuantity,

        Boolean isActive,

        @Size(max = 500)
        String imageUrl
) {}
