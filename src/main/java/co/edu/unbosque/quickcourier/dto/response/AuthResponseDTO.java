package co.edu.unbosque.quickcourier.dto.response;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserResponseDTO user
) {}
