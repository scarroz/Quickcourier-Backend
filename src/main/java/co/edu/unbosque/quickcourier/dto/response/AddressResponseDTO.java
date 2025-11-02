package co.edu.unbosque.quickcourier.dto.response;

import java.time.LocalDateTime;

public record AddressResponseDTO(
        Long id,
        String addressLine1,
        String addressLine2,
        String city,
        String zone,
        String postalCode,
        Boolean isDefault,
        String fullAddress,
        LocalDateTime createdAt
) {}