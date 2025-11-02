package co.edu.unbosque.quickcourier.service.impl;

import co.edu.unbosque.quickcourier.dto.request.LoginRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.RefreshTokenRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.RegisterRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.AuthResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.UserResponseDTO;
import co.edu.unbosque.quickcourier.exception.BadRequestException;
import co.edu.unbosque.quickcourier.exception.ResourceNotFoundException;
import co.edu.unbosque.quickcourier.model.RefreshToken;
import co.edu.unbosque.quickcourier.model.TokenBlackList;
import co.edu.unbosque.quickcourier.model.User;
import co.edu.unbosque.quickcourier.model.UserRole;
import co.edu.unbosque.quickcourier.repository.RefreshTokenRepository;
import co.edu.unbosque.quickcourier.repository.TokenBlackListRepository;
import co.edu.unbosque.quickcourier.repository.UserRepository;
import co.edu.unbosque.quickcourier.security.JwtTokenProvider;
import co.edu.unbosque.quickcourier.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Implementación del servicio de autenticación
 * Maneja login, registro, refresh token y logout
 */
@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlackListRepository tokenBlacklistRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           TokenBlackListRepository tokenBlacklistRepository,
                           JwtTokenProvider jwtTokenProvider,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        logger.info("Login attempt for email: {}", request.email());

        // Buscar usuario
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validar password
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            logger.warn("Invalid password for email: {}", request.email());
            throw new BadRequestException("Credenciales inválidas");
        }

        // Validar que esté activo
        if (!user.getIsActive()) {
            throw new BadRequestException("Cuenta inactiva");
        }

        // Actualizar último login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generar tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getEmail(),
                user.getId()
        );

        // Guardar refresh token
        saveRefreshToken(user, refreshToken);

        logger.info("Login successful for user: {}", user.getEmail());

        // Crear UserResponseDTO
        UserResponseDTO userDTO = new UserResponseDTO(
                user.getId(),
                user.getUuid(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole().name(),
                user.getIsActive(),
                user.getCreatedAt()
        );

        return new AuthResponseDTO(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationInSeconds(),
                userDTO
        );
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        logger.info("Registration attempt for email: {}", request.email());

        // Verificar si el email ya existe
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("El email ya está registrado");
        }

        // Crear nuevo usuario
        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        user.setRole(UserRole.CUSTOMER); // Por defecto CUSTOMER
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Generar tokens
        String accessToken = jwtTokenProvider.generateAccessToken(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getRole().name()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                savedUser.getEmail(),
                savedUser.getId()
        );

        // Guardar refresh token
        saveRefreshToken(savedUser, refreshToken);

        logger.info("User registered successfully: {}", savedUser.getEmail());

        // Crear UserResponseDTO
        UserResponseDTO userDTO = new UserResponseDTO(
                savedUser.getId(),
                savedUser.getUuid(),
                savedUser.getEmail(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getPhone(),
                savedUser.getRole().name(),
                savedUser.getIsActive(),
                savedUser.getCreatedAt()
        );

        return new AuthResponseDTO(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationInSeconds(),
                userDTO
        );
    }

    @Override
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        logger.info("Refresh token request");

        String refreshToken = request.refreshToken();

        // Validar refresh token
        if (!jwtTokenProvider.isTokenValid(refreshToken)) {
            throw new BadRequestException("Refresh token inválido o expirado");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Token proporcionado no es un refresh token");
        }

        // Extraer usuario
        Long userId = jwtTokenProvider.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Verificar que el refresh token existe en BD
        String tokenHash = String.valueOf(refreshToken.hashCode());
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHashAndUserId(tokenHash, userId)
                .orElseThrow(() -> new BadRequestException("Refresh token no válido"));

        if (storedToken.getIsRevoked()) {
            throw new BadRequestException("Refresh token revocado");
        }

        // Generar nuevo access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getEmail(),
                user.getId(),
                user.getRole().name()
        );

        logger.info("Access token refreshed for user: {}", user.getEmail());

        // Crear UserResponseDTO
        UserResponseDTO userDTO = new UserResponseDTO(
                user.getId(),
                user.getUuid(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole().name(),
                user.getIsActive(),
                user.getCreatedAt()
        );

        return new AuthResponseDTO(
                newAccessToken,
                refreshToken, // Mismo refresh token
                "Bearer",
                jwtTokenProvider.getAccessTokenExpirationInSeconds(),
                userDTO
        );
    }

    @Override
    public void logout(String token) {
        logger.info("Logout request");

        if (token == null || !jwtTokenProvider.isTokenValid(token)) {
            throw new BadRequestException("Token inválido");
        }

        Long userId = jwtTokenProvider.extractUserId(token);

        // Agregar a blacklist
        TokenBlackList blacklistedToken = new TokenBlackList();
        blacklistedToken.setJti(String.valueOf(token.hashCode()));
        // CORRECCIÓN: Usar el usuario correctamente
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        blacklistedToken.setUser(user);
        blacklistedToken.setExpiresAt(jwtTokenProvider.getExpirationDateAsLocalDateTime(token));
        blacklistedToken.setRevokedAt(LocalDateTime.now());
        blacklistedToken.setReason("LOGOUT");

        tokenBlacklistRepository.save(blacklistedToken);

        // Revocar todos los refresh tokens del usuario
        revokeAllUserTokens(userId);

        logger.info("User {} logged out successfully", userId);
    }

    @Override
    public void revokeAllUserTokens(Long userId) {
        logger.info("Revoking all tokens for user: {}", userId);

        refreshTokenRepository.revokeAllUserTokens(
                userId,
                LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
        );

        logger.info("All tokens revoked for user: {}", userId);
    }

    /**
     * Guarda el refresh token en la base de datos
     */
    private void saveRefreshToken(User user, String refreshToken) {
        String tokenHash = String.valueOf(refreshToken.hashCode());

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(tokenHash);
        token.setExpiresAt(jwtTokenProvider.getExpirationDateAsLocalDateTime(refreshToken));
        token.setIsRevoked(false);
        token.setCreatedAt(LocalDateTime.now());

        refreshTokenRepository.save(token);
    }
}