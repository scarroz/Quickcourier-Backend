package co.edu.unbosque.quickcourier.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filtro de Rate Limiting usando Caffeine Cache
 * Limita el número de requests por IP o usuario en una ventana de tiempo
 *
 * KISS: Simple sliding window con Caffeine
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    // Cache: Key = identifier (IP o userId), Value = contador de requests
    private final Cache<String, AtomicInteger> requestCounts;

    @Value("${rate-limit.requests-per-minute:100}")
    private int maxRequestsPerMinute;

    public RateLimitingFilter() {
        // Cache que expira entradas después de 1 minuto
        this.requestCounts = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .maximumSize(10000)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Obtener identificador (IP o userId)
        String identifier = getIdentifier(request);

        // Obtener o crear contador
        AtomicInteger count = requestCounts.get(identifier, k -> new AtomicInteger(0));

        int currentCount = count.incrementAndGet();

        // Agregar headers de rate limit
        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequestsPerMinute));
        response.setHeader("X-RateLimit-Remaining",
                String.valueOf(Math.max(0, maxRequestsPerMinute - currentCount)));

        // Verificar si excede el límite
        if (currentCount > maxRequestsPerMinute) {
            logger.warn("Rate limit exceeded for identifier: {} (count: {})",
                    identifier, currentCount);

            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Too Many Requests\"," +
                            "\"message\":\"Rate limit exceeded. Try again later.\"," +
                            "\"limit\":" + maxRequestsPerMinute + "}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Obtiene el identificador para rate limiting
     * Prioridad: userId > API Key > IP
     */
    private String getIdentifier(HttpServletRequest request) {
        // Si hay userId autenticado, usar ese
        Object userId = request.getAttribute("userId");
        if (userId != null) {
            return "user:" + userId;
        }

        // Si hay API Key, usar ese
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isEmpty()) {
            return "apikey:" + apiKey.substring(0, Math.min(10, apiKey.length()));
        }

        // Fallback a IP
        String ip = getClientIP(request);
        return "ip:" + ip;
    }

    /**
     * Obtiene la IP real del cliente considerando proxies
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Método para limpiar el cache manualmente si es necesario
     */
    public void clearCache() {
        requestCounts.invalidateAll();
        logger.info("Rate limit cache cleared");
    }

    /**
     * Obtiene estadísticas del cache
     */
    public long getCacheSize() {
        return requestCounts.estimatedSize();
    }
}