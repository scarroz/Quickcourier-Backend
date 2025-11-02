package co.edu.unbosque.quickcourier.service;

import co.edu.unbosque.quickcourier.dto.request.CreatePaymentRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.PaymentResponseDTO;

import java.util.List;

public interface PaymentService {
    PaymentResponseDTO createPayment(CreatePaymentRequestDTO request, Long userId);
    PaymentResponseDTO getPaymentById(Long id);
    List<PaymentResponseDTO> getOrderPayments(Long orderId);
    PaymentResponseDTO processPayment(Long paymentId);
    PaymentResponseDTO refundPayment(Long paymentId);
}
