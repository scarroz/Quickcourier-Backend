package co.edu.unbosque.quickcourier.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Configuración de Caffeine Cache (Nivel 1 - In-Memory)
 * Cache ultra-rápido en memoria para datos frecuentemente accedidos
 */
@Configuration
@EnableCaching
public class CaffeineConfig {

    /**
     * Cache Manager de Caffeine como caché primario (L1)
     * - Ultra rápido (microsegundos)
     * - Local al servidor
     * - Ideal para datos que cambian poco
     */
    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "products",
                "productsBySku",
                "categories",


                "shippingRules",
                "shippingExtras",

                "orders",
                "recentOrders",
                "orderSummaries",

                // 📦 Productos activos
                "activeProducts"
        );

        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Configuración de Caffeine con políticas de eviction
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                // Tamaño máximo del cache
                .maximumSize(1000)
                // Expiración después de escritura
                .expireAfterWrite(10, TimeUnit.MINUTES)
                // Expiración después de acceso (refresh si no se usa)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                // Habilitar estadísticas para monitoreo
                .recordStats()
                // Tamaño inicial
                .initialCapacity(100);
    }

    /**
     * Cache específico para productos con TTL más largo
     * Los productos cambian menos frecuentemente
     */
    @Bean("productCacheManager")
    public CacheManager productCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("products", "productsBySku");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }

    /**
     * Cache para reglas de envío con refresh automático
     * Las reglas cambian frecuentemente según promociones
     */
    @Bean("shippingRuleCacheManager")
    public CacheManager shippingRuleCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("shippingRules");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.MINUTES) // Refresh más frecuente
                .recordStats());
        return cacheManager;
    }
}
