package co.edu.unbosque.quickcourier.dto.response;

import java.math.BigDecimal;

public record OrderItemResponseDTO(
        Long id,
        ProductSummaryResponseDTO product,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        BigDecimal weightKg
) {}
