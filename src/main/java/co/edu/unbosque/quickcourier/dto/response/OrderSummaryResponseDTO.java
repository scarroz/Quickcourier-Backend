package co.edu.unbosque.quickcourier.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryResponseDTO(
        Long id,
        String orderNumber,
        BigDecimal totalAmount,
        String status,
        String paymentStatus,
        Integer itemsCount,
        LocalDateTime createdAt
) {}
