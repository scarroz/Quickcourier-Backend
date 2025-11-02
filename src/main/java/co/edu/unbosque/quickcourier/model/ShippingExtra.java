package co.edu.unbosque.quickcourier.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un extra de envío disponible
 * (Exprés, Frágil, Seguro, Empaque de Regalo, etc.)
 */
@Entity
@Table(name = "shipping_extra", indexes = {
        @Index(name = "idx_shipping_extra_code", columnList = "code")
})
public class ShippingExtra {

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

    @NotNull(message = "Precio base es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @NotNull(message = "Tipo de precio es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false, length = 20)
    private PriceType priceType = PriceType.FIXED;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "percentage_value", precision = 5, scale = 2)
    private BigDecimal percentageValue;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ShippingExtra() {
    }

    public ShippingExtra(String code, String name, BigDecimal basePrice, PriceType priceType) {
        this.code = code;
        this.name = name;
        this.basePrice = basePrice;
        this.priceType = priceType;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
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

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public PriceType getPriceType() {
        return priceType;
    }

    public void setPriceType(PriceType priceType) {
        this.priceType = priceType;
    }

    public BigDecimal getPercentageValue() {
        return percentageValue;
    }

    public void setPercentageValue(BigDecimal percentageValue) {
        this.percentageValue = percentageValue;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods

    public BigDecimal calculatePrice(BigDecimal orderSubtotal) {
        return switch (priceType) {
            case FIXED -> basePrice;
            case PERCENTAGE -> {
                if (percentageValue == null) {
                    yield BigDecimal.ZERO;
                }
                yield orderSubtotal.multiply(percentageValue)
                        .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            }
        };
    }

    @Override
    public String toString() {
        return "ShippingExtra{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", priceType=" + priceType +
                ", isActive=" + isActive +
                '}';
    }
}