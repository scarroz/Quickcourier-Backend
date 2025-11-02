package co.edu.unbosque.quickcourier.service.impl;

import co.edu.unbosque.quickcourier.decorator.OrderComponent;
import co.edu.unbosque.quickcourier.decorator.OrderDecoratorBuilder;
import co.edu.unbosque.quickcourier.mapper.DataMapper;
import co.edu.unbosque.quickcourier.dto.request.CreateOrderRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.OrderResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.OrderSummaryResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.PageResponseDTO;
import co.edu.unbosque.quickcourier.exception.BadRequestException;
import co.edu.unbosque.quickcourier.exception.ResourceNotFoundException;
import co.edu.unbosque.quickcourier.exception.UnauthorizedException;
import co.edu.unbosque.quickcourier.factory.OrderFactory;
import co.edu.unbosque.quickcourier.model.*;
import co.edu.unbosque.quickcourier.repository.*;
import co.edu.unbosque.quickcourier.service.OrderService;
import co.edu.unbosque.quickcourier.strategy.ShippingStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final ShippingExtraRepository shippingExtraRepository;
    private final OrderFactory orderFactory;
    private final ShippingStrategyFactory shippingStrategyFactory;
    private final OrderDecoratorBuilder decoratorBuilder;
    private final DataMapper dataMapper;


    public OrderServiceImpl(OrderRepository orderRepository,
                            UserRepository userRepository,
                            AddressRepository addressRepository,
                            ProductRepository productRepository,
                            ShippingExtraRepository shippingExtraRepository,
                            OrderFactory orderFactory,
                            ShippingStrategyFactory shippingStrategyFactory,
                            OrderDecoratorBuilder decoratorBuilder,
                            DataMapper dataMapper) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.shippingExtraRepository = shippingExtraRepository;
        this.orderFactory = orderFactory;
        this.shippingStrategyFactory = shippingStrategyFactory;
        this.decoratorBuilder = decoratorBuilder;
        this.dataMapper = dataMapper;
    }

    @Override
    @CacheEvict(value = {"recentOrders", "orderSummaries"}, allEntries = true)
    public OrderResponseDTO createOrder(CreateOrderRequestDTO request, Long userId) {
        logger.info("Creating order for user {} with {} items", userId, request.items().size());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new ResourceNotFoundException("Direccion no encontrada"));

        if (!address.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("La direccion no pertenece al usuario");
        }

        // FACTORY PATTERN: Crear pedido base con items
        List<OrderFactory.ProductQuantity> productQuantities = request.items().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.productId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Producto no encontrado: " + item.productId()));
                    return new OrderFactory.ProductQuantity(product, item.quantity());
                })
                .collect(Collectors.toList());

        Order order = orderFactory.createOrderWithItems(user, address, productQuantities);

        logger.debug("Order base created: subtotal=${}, weight={}kg",
                order.getSubtotal(), order.getTotalWeightKg());

        // STRATEGY PATTERN: Calcular costo de envio
        var shippingResult = shippingStrategyFactory.calculateShipping(order);
        order.setShippingCost(shippingResult.shippingCost());
        order.setAppliedShippingRuleCode(shippingResult.appliedRuleCode());

        logger.info("Shipping calculated: ${} using rule: {}",
                shippingResult.shippingCost(), shippingResult.appliedRuleCode());

        // DECORATOR PATTERN: Aplicar extras
        if (request.extraCodes() != null && !request.extraCodes().isEmpty()) {
            applyOrderExtras(order, request.extraCodes());
        }

        // Calcular totales finales: subtotal + envio + extras + impuestos (IVA 19%)
        order.calculateTotals();

        logger.info("Order totals: subtotal=${}, shipping=${}, extras=${}, tax=${}, TOTAL=${}",
                order.getSubtotal(), order.getShippingCost(), order.getExtrasCost(),
                order.getTaxAmount(), order.getTotalAmount());

        // Guardar pedido
        Order savedOrder = orderRepository.save(order);

        // Decrementar stock de productos
        savedOrder.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.decreaseStock(item.getQuantity());
            productRepository.save(product);
        });

        logger.info("Order {} created successfully. Total: ${}",
                savedOrder.getOrderNumber(), savedOrder.getTotalAmount());

        return dataMapper.toOrderResponseDTO(savedOrder);
    }

    @Override
    @Cacheable(value = "orders", key = "#id")
    public OrderResponseDTO getOrderById(Long id, Long userId) {
        logger.debug("Fetching order by id: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        validateOrderOwnership(order, userId);

        return dataMapper.toOrderResponseDTO(order);
    }

    @Override
    @Cacheable(value = "orders", key = "#orderNumber")
    public OrderResponseDTO getOrderByNumber(String orderNumber, Long userId) {
        logger.debug("Fetching order by number: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        validateOrderOwnership(order, userId);

        return dataMapper.toOrderResponseDTO(order);
    }

    @Override
    @Cacheable(value = "orderSummaries", key = "#userId + '_' + #pageable.pageNumber")
    public PageResponseDTO<OrderSummaryResponseDTO> getUserOrders(Long userId, Pageable pageable) {
        logger.debug("Fetching orders for user: {}", userId);

        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return dataMapper.toOrderSummaryPageResponseDTO(orders);
    }

    @Override
    public PageResponseDTO<OrderSummaryResponseDTO> getOrdersByStatus(String status, Pageable pageable) {
        logger.debug("Fetching orders by status: {}", status);

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            Page<Order> orders = orderRepository.findByStatus(orderStatus, pageable);
            return dataMapper.toOrderSummaryPageResponseDTO(orders);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado de pedido invalido: " + status);
        }
    }

    @Override
    @CacheEvict(value = {"orders", "orderSummaries"}, allEntries = true)
    public OrderResponseDTO confirmOrder(Long id, Long userId) {
        logger.info("Confirming order: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        validateOrderOwnership(order, userId);

        order.confirm();
        Order savedOrder = orderRepository.save(order);

        logger.info("Order {} confirmed", savedOrder.getOrderNumber());

        return dataMapper.toOrderResponseDTO(savedOrder);
    }

    @Override
    @CacheEvict(value = {"orders", "orderSummaries"}, allEntries = true)
    public OrderResponseDTO cancelOrder(Long id, Long userId) {
        logger.info("Cancelling order: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        validateOrderOwnership(order, userId);

        order.cancel();
        Order savedOrder = orderRepository.save(order);

        // Restaurar stock
        savedOrder.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.increaseStock(item.getQuantity());
            productRepository.save(product);
        });

        logger.info("Order {} cancelled and stock restored", savedOrder.getOrderNumber());

        return dataMapper.toOrderResponseDTO(savedOrder);
    }

    @Override
    @CacheEvict(value = {"orders", "orderSummaries"}, allEntries = true)
    public OrderResponseDTO recalculateOrderWithExtras(Long orderId, List<String> newExtraCodes, Long userId) {
        logger.info("Recalculating order {} with new extras", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));

        validateOrderOwnership(order, userId);

        if (!order.canBeCancelled()) {
            throw new BadRequestException("No se puede modificar un pedido en estado: " + order.getStatus());
        }

        // Limpiar extras existentes
        order.getExtras().clear();
        order.setExtrasCost(BigDecimal.ZERO);

        // Aplicar nuevos extras
        if (newExtraCodes != null && !newExtraCodes.isEmpty()) {
            applyOrderExtras(order, newExtraCodes);
        }

        // Recalcular totales
        order.calculateTotals();

        Order savedOrder = orderRepository.save(order);

        logger.info("Order {} recalculated. New total: ${}",
                savedOrder.getOrderNumber(), savedOrder.getTotalAmount());

        return dataMapper.toOrderResponseDTO(savedOrder);
    }

    /**
     * Aplica extras al pedido usando Decorator Pattern
     */
    private void applyOrderExtras(Order order, List<String> extraCodes) {
        OrderComponent decoratedOrder = decoratorBuilder.buildWithExtras(order, extraCodes);

        BigDecimal baseOrderCost = order.getSubtotal().add(order.getShippingCost());
        BigDecimal decoratedCost = decoratedOrder.getCost();
        BigDecimal extrasCost = decoratedCost.subtract(baseOrderCost);

        order.setExtrasCost(extrasCost);

        logger.debug("Extras applied: {}. Total extras cost: ${}",
                decoratedOrder.getAppliedExtras(), extrasCost);

        // Crear relaciones OrderExtra
        List<ShippingExtra> extras = shippingExtraRepository.findByCodesAndActive(extraCodes);

        for (ShippingExtra extra : extras) {
            BigDecimal appliedPrice = extra.calculatePrice(order.getSubtotal());
            OrderExtra orderExtra = new OrderExtra(order, extra, appliedPrice);
            order.addExtra(orderExtra);
        }
    }

    /**
     * Valida que el pedido pertenezca al usuario
     */
    private void validateOrderOwnership(Order order, Long userId) {
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tiene permisos para acceder a este pedido");
        }
    }
}