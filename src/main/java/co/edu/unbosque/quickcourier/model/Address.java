package co.edu.unbosque.quickcourier.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entidad que representa una dirección de entrega
 */
@Entity
@Table(name = "address", indexes = {
        @Index(name = "idx_address_user_id", columnList = "user_id"),
        @Index(name = "idx_address_zone", columnList = "zone")
})
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_address_user"))
    private User user;

    @NotBlank(message = "Dirección línea 1 es obligatoria")
    @Size(max = 255, message = "Dirección línea 1 no puede exceder 255 caracteres")
    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Size(max = 255, message = "Dirección línea 2 no puede exceder 255 caracteres")
    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @NotBlank(message = "Ciudad es obligatoria")
    @Size(max = 100, message = "Ciudad no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String city;

    @NotBlank(message = "Zona es obligatoria")
    @Size(max = 50, message = "Zona no puede exceder 50 caracteres")
    @Column(nullable = false, length = 50)
    private String zone;

    @Size(max = 20, message = "Código postal no puede exceder 20 caracteres")
    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Address() {
    }

    public Address(User user, String addressLine1, String city, String zone) {
        this.user = user;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.zone = zone;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Helper methods

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(addressLine1);
        if (addressLine2 != null && !addressLine2.isBlank()) {
            sb.append(", ").append(addressLine2);
        }
        sb.append(", ").append(city);
        if (postalCode != null && !postalCode.isBlank()) {
            sb.append(" ").append(postalCode);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", addressLine1='" + addressLine1 + '\'' +
                ", city='" + city + '\'' +
                ", zone='" + zone + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}