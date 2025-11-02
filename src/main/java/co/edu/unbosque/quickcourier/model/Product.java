package co.edu.unbosque.quickcourier.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un producto del catálogo
 */
@Entity
@Table(name = "product", indexes = {
        @Index(name = "idx_products_sku", columnList = "sku"),
        @Index(name = "idx_products_category", columnList = "category_id")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "SKU es obligatorio")
    @Size(max = 50, message = "SKU no puede exceder 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @NotBlank(message = "Nombre del producto es obligatorio")
    @Size(max = 255, message = "Nombre no puede exceder 255 caracteres")
    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_products_category"))
    private Category category;

    @NotNull(message = "Precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "Precio debe ser mayor o igual a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Peso es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "Peso debe ser mayor a 0")
    @Column(name = "weight_kg", nullable = false, precision = 8, scale = 3)
    private BigDecimal weightKg;

    @Min(value = 0, message = "Stock no puede ser negativo")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Size(max = 500, message = "URL de imagen no puede exceder 500 caracteres")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Product() {
    }

    public Product(String sku, String name, BigDecimal price, BigDecimal weightKg) {
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.weightKg = weightKg;
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

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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


    public boolean hasStock(int quantity) {
        return this.stockQuantity >= quantity;
    }

    public void decreaseStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + name);
        }
        this.stockQuantity -= quantity;
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }
}