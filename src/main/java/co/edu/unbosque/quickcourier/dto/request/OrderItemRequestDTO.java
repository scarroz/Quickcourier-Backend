package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDTO(
        @NotNull(message = "ID de producto es obligatorio")
        Long productId,

        @NotNull(message = "Cantidad es obligatoria")
        @Min(value = 1, message = "Cantidad debe ser al menos 1")
        Integer quantity
) {}
