package co.edu.unbosque.quickcourier.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDTO(
        Long id,
        String orderNumber,
        UserResponseDTO user,
        AddressResponseDTO address,
        BigDecimal subtotal,
        BigDecimal shippingCost,
        BigDecimal extrasCost,
        BigDecimal taxRate,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        String status,
        String paymentStatus,
        BigDecimal totalWeightKg,
        String appliedShippingRuleCode,
        List<OrderItemResponseDTO> items,
        List<OrderExtraResponseDTO> extras,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime confirmedAt,
        LocalDateTime deliveredAt,
        LocalDateTime cancelledAt
) {}
