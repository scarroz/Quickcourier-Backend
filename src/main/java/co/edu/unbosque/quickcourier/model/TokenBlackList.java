package co.edu.unbosque.quickcourier.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entidad que representa tokens JWT revocados antes de su expiración natural
 */
@Entity
@Table(name = "token_blacklist", indexes = {
        @Index(name = "idx_token_blacklist_jti", columnList = "jti"),
        @Index(name = "idx_token_blacklist_expires_at", columnList = "expires_at"),
        @Index(name = "idx_token_blacklist_user_id", columnList = "user_id")
})
public class TokenBlackList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "JTI es obligatorio")
    @Size(max = 255)
    @Column(nullable = false, unique = true, length = 255)
    private String jti;

    @NotNull(message = "Usuario es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_token_blacklist_user"))
    private User user;

    @NotNull(message = "Fecha de expiración es obligatoria")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at", nullable = false)
    private LocalDateTime revokedAt;

    @Size(max = 100)
    @Column(length = 100)
    private String reason;

    public TokenBlackList() {
    }

    public TokenBlackList(String jti, User user, LocalDateTime expiresAt, String reason) {
        this.jti = jti;
        this.user = user;
        this.expiresAt = expiresAt;
        this.reason = reason;
        this.revokedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.revokedAt == null) {
            this.revokedAt = LocalDateTime.now();
        }
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    // Helper methods

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    @Override
    public String toString() {
        return "TokenBlacklist{" +
                "id=" + id +
                ", jti='" + jti + '\'' +
                ", reason='" + reason + '\'' +
                ", revokedAt=" + revokedAt +
                '}';
    }
}