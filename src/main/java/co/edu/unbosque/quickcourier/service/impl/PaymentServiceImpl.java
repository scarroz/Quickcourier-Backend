package co.edu.unbosque.quickcourier.service.impl;

import co.edu.unbosque.quickcourier.dto.request.CreatePaymentRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.PaymentResponseDTO;
import co.edu.unbosque.quickcourier.exception.BadRequestException;
import co.edu.unbosque.quickcourier.exception.ResourceNotFoundException;
import co.edu.unbosque.quickcourier.exception.UnauthorizedException;
import co.edu.unbosque.quickcourier.mapper.DataMapper;
import co.edu.unbosque.quickcourier.model.Order;
import co.edu.unbosque.quickcourier.model.Payment;
import co.edu.unbosque.quickcourier.model.PaymentStatus;
import co.edu.unbosque.quickcourier.repository.OrderRepository;
import co.edu.unbosque.quickcourier.repository.PaymentRepository;
import co.edu.unbosque.quickcourier.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de pagos
 * Simula procesamiento de pagos
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final DataMapper dataMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OrderRepository orderRepository,
                              DataMapper dataMapper) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.dataMapper = dataMapper;
    }

    @Override
    public PaymentResponseDTO createPayment(CreatePaymentRequestDTO request, Long userId) {
        logger.info("Creating payment for order: {}", request.orderId());

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        // Validar que el pedido pertenezca al usuario
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("No tiene permisos para pagar este pedido");
        }

        // Validar que el pedido esté en estado CONFIRMED
        if (!order.getStatus().name().equals("CONFIRMED")) {
            throw new BadRequestException("El pedido debe estar confirmado para realizar el pago");
        }

        // Validar que no haya un pago completado
        List<Payment> existingPayments = paymentRepository.findByOrderId(order.getId());
        boolean hasCompletedPayment = existingPayments.stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.PAID);

        if (hasCompletedPayment) {
            throw new BadRequestException("El pedido ya tiene un pago completado");
        }

        // Crear pago
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setTransactionId(generateTransactionId());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

       /* if (request.gatewayResponse() != null) {
            // En un sistema real, esto vendría del gateway de pago
            payment.setGatewayResponse(request.gatewayResponse());
        }
*/
        Payment savedPayment = paymentRepository.save(payment);

        logger.info("Payment created for order {}: transaction {}",
                order.getOrderNumber(), savedPayment.getTransactionId());

        return dataMapper.toPaymentResponseDTO(savedPayment);
    }

    @Override
    public PaymentResponseDTO getPaymentById(Long id) {
        logger.debug("Fetching payment: {}", id);

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));

        return dataMapper.toPaymentResponseDTO(payment);
    }

    @Override
    public List<PaymentResponseDTO> getOrderPayments(Long orderId) {
        logger.debug("Fetching payments for order: {}", orderId);

        // Verificar que la orden existe
        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException("Orden no encontrada");
        }

        List<Payment> payments = paymentRepository.findByOrderId(orderId);

        return payments.stream()
                .map(dataMapper::toPaymentResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponseDTO processPayment(Long paymentId) {
        logger.info("Processing payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Solo se pueden procesar pagos pendientes");
        }

        // SIMULACIÓN: En un sistema real, aquí se integraría con el gateway de pago
        // (Ej: PayU, Mercado Pago, Stripe, etc.)
        boolean paymentSuccessful = simulatePaymentGateway();

        if (paymentSuccessful) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setCompletedAt(LocalDateTime.now());

            // Actualizar estado del pedido
            Order order = payment.getOrder();
            order.setPaymentStatus(co.edu.unbosque.quickcourier.model.PaymentStatus.PAID);
            orderRepository.save(order);

            logger.info("Payment {} completed successfully", payment.getTransactionId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            logger.warn("Payment {} failed", payment.getTransactionId());
        }

        Payment updatedPayment = paymentRepository.save(payment);

        return dataMapper.toPaymentResponseDTO(updatedPayment);
    }

    @Override
    public PaymentResponseDTO refundPayment(Long paymentId) {
        logger.info("Refunding payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Solo se pueden reembolsar pagos completados");
        }

        // SIMULACIÓN: Integración con gateway para reembolso
        payment.setStatus(PaymentStatus.REFUNDED);

        // Actualizar estado del pedido
        Order order = payment.getOrder();
        order.setPaymentStatus(co.edu.unbosque.quickcourier.model.PaymentStatus.REFUNDED);
        orderRepository.save(order);

        Payment refundedPayment = paymentRepository.save(payment);

        logger.info("Payment {} refunded", payment.getTransactionId());

        return dataMapper.toPaymentResponseDTO(refundedPayment);
    }

    /**
     * Genera un ID de transacción único
     */
    private String generateTransactionId() {
        return "QC-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * SIMULACIÓN de gateway de pago
     * En producción, esto sería una llamada real a PayU, Stripe, etc.
     */
    private boolean simulatePaymentGateway() {
        // 95% de probabilidad de éxito (simulación)
        return Math.random() < 0.95;
    }
}