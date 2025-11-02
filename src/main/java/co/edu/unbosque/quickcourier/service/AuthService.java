package co.edu.unbosque.quickcourier.service;

import co.edu.unbosque.quickcourier.dto.request.LoginRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.RefreshTokenRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.RegisterRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO request);
    AuthResponseDTO register(RegisterRequestDTO request);
    AuthResponseDTO refreshToken(RefreshTokenRequestDTO request);
    void logout(String token);
    void revokeAllUserTokens(Long userId);
}
