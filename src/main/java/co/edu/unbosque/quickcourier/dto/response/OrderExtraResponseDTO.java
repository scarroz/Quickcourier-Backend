package co.edu.unbosque.quickcourier.dto.response;

import java.math.BigDecimal;

public record OrderExtraResponseDTO(
        Long id,
        ShippingExtraResponseDTO shippingExtra,
        BigDecimal appliedPrice
) {}