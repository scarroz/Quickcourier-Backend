package co.edu.unbosque.quickcourier.config;

import co.edu.unbosque.quickcourier.exception.RateLimitExceededException;
import co.edu.unbosque.quickcourier.model.RateLimitTracking;
import co.edu.unbosque.quickcourier.model.User;
import co.edu.unbosque.quickcourier.repository.RateLimitTrackingRepository;
import co.edu.unbosque.quickcourier.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Interceptor para control de rate limiting por usuario
 * Limita el número de requests que un usuario puede hacer en una ventana de tiempo
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${rate-limit.window-size-minutes:1}")
    private int windowSizeMinutes;

    private final RateLimitTrackingRepository rateLimitRepository;
    private final UserRepository userRepository;

    public RateLimitInterceptor(RateLimitTrackingRepository rateLimitRepository,
                                UserRepository userRepository) {
        this.rateLimitRepository = rateLimitRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Si rate limiting está deshabilitado, permitir el request
        if (!rateLimitEnabled) {
            return true;
        }

        // Obtener usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            // Si no hay autenticación, permitir (será manejado por Security)
            return true;
        }

        String username = authentication.getName();
        String endpoint = request.getRequestURI();

        // Buscar usuario
        Optional<User> userOpt = userRepository.findByEmail(username);
        if (userOpt.isEmpty()) {
            return true; // Usuario no encontrado, permitir (Security lo maneja)
        }

        User user = userOpt.get();
        LocalDateTime now = LocalDateTime.now();

        // Buscar ventana activa
        Optional<RateLimitTracking> trackingOpt = rateLimitRepository
                .findActiveWindow(user.getId(), endpoint, now);

        if (trackingOpt.isPresent()) {
            RateLimitTracking tracking = trackingOpt.get();

            // Verificar si excede el límite
            if (tracking.getRequestCount() >= requestsPerMinute) {
                logger.warn("Rate limit exceeded for user {} on endpoint {}. Count: {}",
                        user.getEmail(), endpoint, tracking.getRequestCount());

                // Agregar headers informativos
                response.addHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
                response.addHeader("X-RateLimit-Remaining", "0");
                response.addHeader("X-RateLimit-Reset", tracking.getWindowEnd().toString());

                throw new RateLimitExceededException(
                        String.format("Límite de %d requests por minuto excedido. " +
                                        "Intente nuevamente después de %s",
                                requestsPerMinute, tracking.getWindowEnd())
                );
            }

            // Incrementar contador
            tracking.incrementCount();
            rateLimitRepository.save(tracking);

            // Agregar headers informativos
            int remaining = requestsPerMinute - tracking.getRequestCount();
            response.addHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            response.addHeader("X-RateLimit-Remaining", String.valueOf(remaining));
            response.addHeader("X-RateLimit-Reset", tracking.getWindowEnd().toString());

        } else {
            // Crear nueva ventana
            LocalDateTime windowStart = now;
            LocalDateTime windowEnd = now.plusMinutes(windowSizeMinutes);

            RateLimitTracking newTracking = new RateLimitTracking(
                    user, endpoint, windowStart, windowEnd
            );
            rateLimitRepository.save(newTracking);

            // Agregar headers informativos
            response.addHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            response.addHeader("X-RateLimit-Remaining", String.valueOf(requestsPerMinute - 1));
            response.addHeader("X-RateLimit-Reset", windowEnd.toString());

            logger.debug("Created new rate limit window for user {} on endpoint {}",
                    user.getEmail(), endpoint);
        }

        return true;
    }
}