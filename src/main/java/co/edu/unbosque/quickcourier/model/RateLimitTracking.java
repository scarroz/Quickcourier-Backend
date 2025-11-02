package co.edu.unbosque.quickcourier.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entidad que rastrea el rate limiting por usuario
 */
@Entity
@Table(name = "rate_limit_tracking", indexes = {
        @Index(name = "idx_rate_limit_user_window", columnList = "user_id, window_start, window_end"),
        @Index(name = "idx_rate_limit_window_end", columnList = "window_end"),
        @Index(name = "idx_rate_limit_endpoint", columnList = "endpoint"),
        @Index(name = "idx_rate_limit_user_endpoint", columnList = "user_id, endpoint, window_start")
})
public class RateLimitTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_rate_limit_tracking_user"))
    private User user;

    @NotBlank(message = "Endpoint es obligatorio")
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String endpoint;

    @NotNull(message = "Contador de requests es obligatorio")
    @Min(value = 1)
    @Column(name = "request_count", nullable = false)
    private Integer requestCount = 1;

    @NotNull(message = "Inicio de ventana es obligatorio")
    @Column(name = "window_start", nullable = false)
    private LocalDateTime windowStart;

    @NotNull(message = "Fin de ventana es obligatorio")
    @Column(name = "window_end", nullable = false)
    private LocalDateTime windowEnd;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public RateLimitTracking() {
    }

    public RateLimitTracking(User user, String endpoint, LocalDateTime windowStart, LocalDateTime windowEnd) {
        this.user = user;
        this.endpoint = endpoint;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.requestCount = 1;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Integer requestCount) {
        this.requestCount = requestCount;
    }

    public LocalDateTime getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(LocalDateTime windowStart) {
        this.windowStart = windowStart;
    }

    public LocalDateTime getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(LocalDateTime windowEnd) {
        this.windowEnd = windowEnd;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods

    public void incrementCount() {
        this.requestCount++;
    }

    public boolean isWithinWindow(LocalDateTime currentTime) {
        return !currentTime.isBefore(windowStart) && !currentTime.isAfter(windowEnd);
    }

    public boolean isExpired(LocalDateTime currentTime) {
        return currentTime.isAfter(windowEnd);
    }

    @Override
    public String toString() {
        return "RateLimitTracking{" +
                "id=" + id +
                ", endpoint='" + endpoint + '\'' +
                ", requestCount=" + requestCount +
                ", windowStart=" + windowStart +
                ", windowEnd=" + windowEnd +
                '}';
    }
}