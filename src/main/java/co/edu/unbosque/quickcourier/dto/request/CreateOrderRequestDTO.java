package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequestDTO(
        @NotNull(message = "ID de dirección es obligatorio")
        Long addressId,

        @NotEmpty(message = "El pedido debe tener al menos un item")
        List<OrderItemRequestDTO> items,

        List<String> extraCodes
) {}