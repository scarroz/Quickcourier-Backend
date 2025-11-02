package co.edu.unbosque.quickcourier.security;

import co.edu.unbosque.quickcourier.repository.TokenBlackListRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro de autenticación JWT
 * Intercepta cada request y valida el token Bearer
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlackListRepository tokenBlacklistRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   TokenBlackListRepository tokenBlacklistRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token != null && jwtTokenProvider.isTokenValid(token)) {

                // Verificar que no esté en la blacklist
                if (isTokenBlacklisted(token)) {
                    logger.warn("Attempted to use blacklisted token");
                    filterChain.doFilter(request, response);
                    return;
                }

                // Extraer información del token
                String email = jwtTokenProvider.extractEmail(token);
                Long userId = jwtTokenProvider.extractUserId(token);
                String role = jwtTokenProvider.extractRole(token);

                // Crear autenticación
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establecer en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Agregar userId como atributo del request para fácil acceso
                request.setAttribute("userId", userId);

                logger.debug("User {} authenticated successfully", email);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
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

    /**
     * Verifica si el token está en la blacklist
     */
    private boolean isTokenBlacklisted(String token) {
        try {
            String email = jwtTokenProvider.extractEmail(token);
            return tokenBlacklistRepository.existsByJtiAndUserId(
                    token.substring(0, Math.min(token.length(), 50)), // Simulación de JTI
                    jwtTokenProvider.extractUserId(token)
            );
        } catch (Exception e) {
            return false;
        }
    }
}