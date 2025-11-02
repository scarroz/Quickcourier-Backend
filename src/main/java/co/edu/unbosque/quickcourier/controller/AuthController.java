package co.edu.unbosque.quickcourier.controller;

import co.edu.unbosque.quickcourier.dto.request.LoginRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.RefreshTokenRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.RegisterRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.AuthResponseDTO;
import co.edu.unbosque.quickcourier.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador de Autenticación
 * Endpoints: login, register, refresh, logout
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Autenticación y gestión de tokens JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica usuario y retorna JWT tokens")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crea una cuenta de cliente")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refrescar access token", description = "Obtiene un nuevo access token usando refresh token")
    public ResponseEntity<AuthResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        AuthResponseDTO response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca los tokens del usuario")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String token = extractToken(request);
        authService.logout(token);
        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }

    /**
     * Extrae el token del header Authorization
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}