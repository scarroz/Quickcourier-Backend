package co.edu.unbosque.quickcourier.dto.response;

import java.time.LocalDateTime;

public record CategoryResponseDTO(
        Long id,
        String name,
        String description,
        Boolean isActive,
        LocalDateTime createdAt
) {}