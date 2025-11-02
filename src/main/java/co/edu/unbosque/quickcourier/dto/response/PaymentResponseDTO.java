package co.edu.unbosque.quickcourier.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDTO(
        Long id,
        Long orderId,
        String transactionId,
        String paymentMethod,
        BigDecimal amount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {}
