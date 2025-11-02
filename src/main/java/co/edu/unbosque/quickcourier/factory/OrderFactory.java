package co.edu.unbosque.quickcourier.factory;

import co.edu.unbosque.quickcourier.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Factory para la creación centralizada y validada de pedidos
 * Implementa el patrón Factory Method para encapsular la complejidad
 * de construcción de pedidos con todas sus validaciones y cálculos
 */
@Component
public class OrderFactory {

    private static final String ORDER_NUMBER_PREFIX = "QC";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Crea un pedido básico con validaciones iniciales
     *
     * @param user Usuario que realiza el pedido
     * @param address Dirección de entrega
     * @return Order creada con número único
     */
    public Order createOrder(User user, Address address) {
        validateUser(user);
        validateAddress(address, user);

        String orderNumber = generateOrderNumber();
        Order order = new Order(orderNumber, user, address);

        return order;
    }

    /**
     * Crea un pedido completo con items, validando stock y calculando totales
     *
     * @param user Usuario que realiza el pedido
     * @param address Dirección de entrega
     * @param productQuantities Lista de productos y cantidades
     * @return Order completa con items
     */
    public Order createOrderWithItems(
            User user,
            Address address,
            List<ProductQuantity> productQuantities) {

        Order order = createOrder(user, address);

        // Validar y agregar items
        for (ProductQuantity pq : productQuantities) {
            validateProductAvailability(pq.product(), pq.quantity());
            OrderItem item = createOrderItem(order, pq.product(), pq.quantity());
            order.addItem(item);
        }

        // Calcular peso total y subtotal
        order.calculateTotals();

        return order;
    }

    /**
     * Crea un pedido completo con items y extras de envío
     *
     * @param user Usuario que realiza el pedido
     * @param address Dirección de entrega
     * @param productQuantities Lista de productos y cantidades
     * @param shippingExtras Lista de extras seleccionados
     * @return Order completa con items y extras
     */
    public Order createOrderWithExtras(
            User user,
            Address address,
            List<ProductQuantity> productQuantities,
            List<ShippingExtra> shippingExtras) {

        Order order = createOrderWithItems(user, address, productQuantities);

        // Agregar extras
        if (shippingExtras != null && !shippingExtras.isEmpty()) {
            for (ShippingExtra extra : shippingExtras) {
                validateShippingExtra(extra);
                BigDecimal appliedPrice = extra.calculatePrice(order.getSubtotal());
                OrderExtra orderExtra = new OrderExtra(order, extra, appliedPrice);
                order.addExtra(orderExtra);
            }
        }

        // Recalcular totales con extras
        order.calculateTotals();

        return order;
    }

    /**
     * Crea un OrderItem con precio capturado al momento de la compra
     */
    private OrderItem createOrderItem(Order order, Product product, Integer quantity) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        item.setWeightKg(product.getWeightKg());
        item.calculateSubtotal();

        return item;
    }

    /**
     * Genera un número único de orden con formato: QC-YYYYMMDD-HHMMSS-XXX
     */
    private String generateOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DATE_FORMAT);
        String timePart = String.format("%02d%02d%02d",
                now.getHour(), now.getMinute(), now.getSecond());
        String randomPart = String.format("%03d", (int)(Math.random() * 1000));

        return String.format("%s-%s-%s-%s", ORDER_NUMBER_PREFIX, datePart, timePart, randomPart);
    }

    /**
     * Valida que el usuario esté activo y pueda realizar pedidos
     */
    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Usuario no puede ser null");
        }
        if (!user.getIsActive()) {
            throw new IllegalStateException(
                    "Usuario inactivo no puede realizar pedidos: " + user.getEmail()
            );
        }
        if (user.getRole() != UserRole.CUSTOMER) {
            throw new IllegalStateException(
                    "Solo usuarios con rol CUSTOMER pueden realizar pedidos"
            );
        }
    }

    /**
     * Valida que la dirección pertenezca al usuario
     */
    private void validateAddress(Address address, User user) {
        if (address == null) {
            throw new IllegalArgumentException("Dirección no puede ser null");
        }
        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException(
                    "La dirección no pertenece al usuario especificado"
            );
        }
    }

    /**
     * Valida que el producto tenga stock suficiente
     */
    private void validateProductAvailability(Product product, Integer quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Producto no puede ser null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Cantidad debe ser mayor a 0"
            );
        }
        if (!product.getIsActive()) {
            throw new IllegalStateException(
                    "Producto no está activo: " + product.getName()
            );
        }
        if (product.getStockQuantity()<quantity) {
            throw new IllegalStateException(
                    String.format("Stock insuficiente para %s. Disponible: %d, Solicitado: %d",
                            product.getName(), product.getStockQuantity(), quantity)
            );
        }
    }

    /**
     * Valida que el extra de envío esté activo
     */
    private void validateShippingExtra(ShippingExtra extra) {
        if (extra == null) {
            throw new IllegalArgumentException("Extra de envío no puede ser null");
        }
        if (!extra.getIsActive()) {
            throw new IllegalStateException(
                    "Extra de envío no está activo: " + extra.getName()
            );
        }
    }

    /**
     * Record helper para agrupar producto y cantidad
     */
    public record ProductQuantity(Product product, Integer quantity) {
        public ProductQuantity {
            if (product == null) {
                throw new IllegalArgumentException("Product no puede ser null");
            }
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Quantity debe ser mayor a 0");
            }
        }
    }
}