package co.edu.unbosque.quickcourier.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(
        Long id,
        UUID uuid,
        String email,
        String firstName,
        String lastName,
        String phone,
        String role,
        Boolean isActive,
        LocalDateTime createdAt
) {}
