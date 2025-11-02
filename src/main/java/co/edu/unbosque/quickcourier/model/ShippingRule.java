package co.edu.unbosque.quickcourier.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entidad que representa una regla de cálculo de envío
 * Soporta configuración dinámica mediante JSONB
 */
@Entity
@Table(name = "shipping_rule", indexes = {
        @Index(name = "idx_shipping_rule_code", columnList = "code"),
        @Index(name = "idx_shipping_rule_priority", columnList = "priority"),
        @Index(name = "idx_shipping_rule_validity", columnList = "valid_from, valid_until")
})
public class ShippingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Código es obligatorio")
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank(message = "Nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Tipo de regla es obligatorio")
    @Size(max = 50)
    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType;

    @NotNull(message = "Prioridad es obligatoria")
    @Column(nullable = false)
    private Integer priority = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull(message = "Configuración es obligatoria")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> configuration = new HashMap<>();

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ShippingRule() {
    }

    public ShippingRule(String code, String name, String ruleType, Integer priority) {
        this.code = code;
        this.name = name;
        this.ruleType = ruleType;
        this.priority = priority;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
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

    public boolean isValidAt(LocalDateTime dateTime) {
        if (validFrom != null && dateTime.isBefore(validFrom)) {
            return false;
        }
        if (validUntil != null && dateTime.isAfter(validUntil)) {
            return false;
        }
        return isActive;
    }

    public boolean isCurrentlyValid() {
        return isValidAt(LocalDateTime.now());
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key, Class<T> type) {
        Object value = configuration.get(key);
        if (value == null) {
            return null;
        }

        if (type == String.class) {
            return (T) value.toString();
        } else if (type == Integer.class && value instanceof Number) {
            return (T) Integer.valueOf(((Number) value).intValue());
        } else if (type == Double.class && value instanceof Number) {
            return (T) Double.valueOf(((Number) value).doubleValue());
        } else if (type == Boolean.class) {
            return (T) Boolean.valueOf(value.toString());
        }

        return type.cast(value);
    }

    @Override
    public String toString() {
        return "ShippingRule{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", ruleType='" + ruleType + '\'' +
                ", priority=" + priority +
                ", isActive=" + isActive +
                '}';
    }
}