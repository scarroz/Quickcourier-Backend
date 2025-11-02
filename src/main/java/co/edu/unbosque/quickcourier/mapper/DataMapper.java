package co.edu.unbosque.quickcourier.mapper;


import co.edu.unbosque.quickcourier.dto.response.*;
import co.edu.unbosque.quickcourier.model.*;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper centralizado para todas las conversiones Entity <-> DTO
 * Unifica todos los mapeos en una sola clase para facilitar mantenimiento
 */
@Component
public class DataMapper {

    // =====================================================
    // USER MAPPINGS
    // =====================================================

    public UserResponseDTO toUserResponseDTO(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponseDTO(
                user.getId(),
                user.getUuid(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole().name(),
                user.getIsActive(),
                user.getCreatedAt()
        );
    }

    // =====================================================
    // ADDRESS MAPPINGS
    // =====================================================

    public AddressResponseDTO toAddressResponseDTO(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressResponseDTO(
                address.getId(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getZone(),
                address.getPostalCode(),
                address.getIsDefault(),
                address.getFullAddress(),
                address.getCreatedAt()
        );
    }

    public List<AddressResponseDTO> toAddressResponseDTOList(List<Address> addresses) {
        if (addresses == null) {
            return List.of();
        }

        return addresses.stream()
                .map(this::toAddressResponseDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // CATEGORY MAPPINGS
    // =====================================================

    public CategoryResponseDTO toCategoryResponseDTO(Category category) {
        if (category == null) {
            return null;
        }

        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIsActive(),
                category.getCreatedAt()
        );
    }

    public List<CategoryResponseDTO> toCategoryResponseDTOList(List<Category> categories) {
        if (categories == null) {
            return List.of();
        }

        return categories.stream()
                .map(this::toCategoryResponseDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // PRODUCT MAPPINGS
    // =====================================================

    public ProductResponseDTO toProductResponseDTO(Product product) {
        if (product == null) {
            return null;
        }

        return new ProductResponseDTO(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                toCategoryResponseDTO(product.getCategory()),
                product.getPrice(),
                product.getWeightKg(),
                product.getStockQuantity(),
                product.getIsActive(),
                product.getImageUrl(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public ProductSummaryResponseDTO toProductSummaryResponseDTO(Product product) {
        if (product == null) {
            return null;
        }

        return new ProductSummaryResponseDTO(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getIsActive(),
                product.getImageUrl()
        );
    }

    public PageResponseDTO<ProductResponseDTO> toProductPageResponseDTO(Page<Product> page) {
        List<ProductResponseDTO> content = page.getContent().stream()
                .map(this::toProductResponseDTO)
                .collect(Collectors.toList());

        return new PageResponseDTO<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    // =====================================================
    // ORDER MAPPINGS
    // =====================================================

    public OrderResponseDTO toOrderResponseDTO(Order order) {
        if (order == null) {
            return null;
        }

        List<OrderItemResponseDTO> itemResponses = order.getItems().stream()
                .map(this::toOrderItemResponseDTO)
                .collect(Collectors.toList());

        List<OrderExtraResponseDTO> extraResponses = order.getExtras().stream()
                .map(this::toOrderExtraResponseDTO)
                .collect(Collectors.toList());

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderNumber(),
                toUserResponseDTO(order.getUser()),
                toAddressResponseDTO(order.getAddress()),
                order.getSubtotal(),
                order.getShippingCost(),
                order.getExtrasCost(),
                order.getTaxRate(),
                order.getTaxAmount(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getPaymentStatus().name(),
                order.getTotalWeightKg(),
                order.getAppliedShippingRuleCode(),
                itemResponses,
                extraResponses,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getConfirmedAt(),
                order.getDeliveredAt(),
                order.getCancelledAt()
        );
    }

    public OrderSummaryResponseDTO toOrderSummaryResponseDTO(Order order) {
        if (order == null) {
            return null;
        }

        return new OrderSummaryResponseDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getPaymentStatus().name(),
                order.getItems().size(),
                order.getCreatedAt()
        );
    }

    public PageResponseDTO<OrderSummaryResponseDTO> toOrderSummaryPageResponseDTO(Page<Order> page) {
        List<OrderSummaryResponseDTO> content = page.getContent().stream()
                .map(this::toOrderSummaryResponseDTO)
                .collect(Collectors.toList());

        return new PageResponseDTO<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    private OrderItemResponseDTO toOrderItemResponseDTO(OrderItem item) {
        if (item == null) {
            return null;
        }

        return new OrderItemResponseDTO(
                item.getId(),
                toProductSummaryResponseDTO(item.getProduct()),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal(),
                item.getWeightKg()
        );
    }

    private OrderExtraResponseDTO toOrderExtraResponseDTO(OrderExtra extra) {
        if (extra == null) {
            return null;
        }

        return new OrderExtraResponseDTO(
                extra.getId(),
                toShippingExtraResponseDTO(extra.getShippingExtra()),
                extra.getAppliedPrice()
        );
    }

    // =====================================================
    // SHIPPING MAPPINGS
    // =====================================================

    public ShippingExtraResponseDTO toShippingExtraResponseDTO(ShippingExtra extra) {
        if (extra == null) {
            return null;
        }

        return new ShippingExtraResponseDTO(
                extra.getId(),
                extra.getCode(),
                extra.getName(),
                extra.getDescription(),
                extra.getBasePrice(),
                extra.getPriceType().name(),
                extra.getPercentageValue(),
                extra.getIsActive(),
                extra.getDisplayOrder()
        );
    }

    public List<ShippingExtraResponseDTO> toShippingExtraResponseDTOList(List<ShippingExtra> extras) {
        if (extras == null) {
            return List.of();
        }

        return extras.stream()
                .map(this::toShippingExtraResponseDTO)
                .collect(Collectors.toList());
    }

    public ShippingRuleResponseDTO toShippingRuleResponseDTO(ShippingRule rule) {
        if (rule == null) {
            return null;
        }

        return new ShippingRuleResponseDTO(
                rule.getId(),
                rule.getCode(),
                rule.getName(),
                rule.getDescription(),
                rule.getRuleType(),
                rule.getPriority(),
                rule.getIsActive(),
                rule.getConfiguration(),
                rule.getValidFrom(),
                rule.getValidUntil(),
                rule.getCreatedAt()
        );
    }

    public List<ShippingRuleResponseDTO> toShippingRuleResponseDTOList(List<ShippingRule> rules) {
        if (rules == null) {
            return List.of();
        }

        return rules.stream()
                .map(this::toShippingRuleResponseDTO)
                .collect(Collectors.toList());
    }

    public ShippingCalculationResponseDTO toShippingCalculationResponseDTO(
            co.edu.unbosque.quickcourier.strategy.ShippingStrategyFactory.ShippingCalculationResult result) {
        if (result == null) {
            return null;
        }

        return new ShippingCalculationResponseDTO(
                result.shippingCost(),
                result.appliedRuleCode(),
                result.appliedRuleName(),
                result.calculationDetails()
        );
    }

    // =====================================================
    // PAYMENT MAPPINGS
    // =====================================================

    public PaymentResponseDTO toPaymentResponseDTO(Payment payment) {
        if (payment == null) {
            return null;
        }

        return new PaymentResponseDTO(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getTransactionId(),
                payment.getPaymentMethod(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getCreatedAt(),
                payment.getCompletedAt()
        );
    }

    public List<PaymentResponseDTO> toPaymentResponseDTOList(List<Payment> payments) {
        if (payments == null) {
            return List.of();
        }

        return payments.stream()
                .map(this::toPaymentResponseDTO)
                .collect(Collectors.toList());
    }

    // =====================================================
    // GENERIC PAGE MAPPING
    // =====================================================

    public <T, D> PageResponseDTO<D> toPageResponseDTO(Page<T> page,
                                                       java.util.function.Function<T, D> mapper) {
        List<D> content = page.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return new PageResponseDTO<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}