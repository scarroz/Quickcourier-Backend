package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateShippingExtraRequestDTO(
        @NotBlank(message = "Código es obligatorio")
        @Size(max = 50)
        String code,

        @NotBlank(message = "Nombre es obligatorio")
        @Size(max = 100)
        String name,

        String description,

        @NotNull(message = "Precio base es obligatorio")
        @DecimalMin(value = "0.0")
        BigDecimal basePrice,

        @NotNull(message = "Tipo de precio es obligatorio")
        String priceType,

        @DecimalMin(value = "0.0")
        BigDecimal percentageValue,

        Integer displayOrder
) {}
