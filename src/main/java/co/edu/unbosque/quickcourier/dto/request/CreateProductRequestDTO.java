package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateProductRequestDTO(
        @NotBlank(message = "SKU es obligatorio")
        @Size(max = 50)
        String sku,

        @NotBlank(message = "Nombre es obligatorio")
        @Size(max = 255)
        String name,

        String description,

        @NotNull(message = "ID de categoría es obligatorio")
        Long categoryId,

        @NotNull(message = "Precio es obligatorio")
        @DecimalMin(value = "0.0", message = "Precio debe ser mayor o igual a 0")
        BigDecimal price,

        @NotNull(message = "Peso es obligatorio")
        @DecimalMin(value = "0.001", message = "Peso debe ser mayor a 0")
        BigDecimal weightKg,

        @Min(value = 0, message = "Stock no puede ser negativo")
        Integer stockQuantity,

        @Size(max = 500)
        String imageUrl
) {}
