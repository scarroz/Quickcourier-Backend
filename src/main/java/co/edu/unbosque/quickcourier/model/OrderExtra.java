package co.edu.unbosque.quickcourier.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa la relación entre un pedido y sus extras aplicados
 */
@Entity
@Table(name = "order_extra",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_order_shipping_extra",
                columnNames = {"order_id", "shipping_extra_id"}
        ),
        indexes = {
                @Index(name = "idx_order_extra_order_id", columnList = "order_id"),
                @Index(name = "idx_order_extra_shipping_extra_id", columnList = "shipping_extra_id")
        }
)
public class OrderExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Pedido es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_extra_order"))
    private Order order;

    @NotNull(message = "Extra de envío es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipping_extra_id", nullable = false, foreignKey = @ForeignKey(name = "fk_order_extra_shipping_extra"))
    private ShippingExtra shippingExtra;

    @NotNull(message = "Precio aplicado es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "applied_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal appliedPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrderExtra() {
    }

    public OrderExtra(Order order, ShippingExtra shippingExtra, BigDecimal appliedPrice) {
        this.order = order;
        this.shippingExtra = shippingExtra;
        this.appliedPrice = appliedPrice;
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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public ShippingExtra getShippingExtra() {
        return shippingExtra;
    }

    public void setShippingExtra(ShippingExtra shippingExtra) {
        this.shippingExtra = shippingExtra;
    }

    public BigDecimal getAppliedPrice() {
        return appliedPrice;
    }

    public void setAppliedPrice(BigDecimal appliedPrice) {
        this.appliedPrice = appliedPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "OrderExtra{" +
                "id=" + id +
                ", appliedPrice=" + appliedPrice +
                ", createdAt=" + createdAt +
                '}';
    }
}