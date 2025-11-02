package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDTO(
        @NotBlank(message = "Refresh token es obligatorio")
        String refreshToken
) {}