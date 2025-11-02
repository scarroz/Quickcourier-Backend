package co.edu.unbosque.quickcourier.service;

import co.edu.unbosque.quickcourier.dto.request.CreateOrderRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.OrderResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.OrderSummaryResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.PageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(CreateOrderRequestDTO request, Long userId);
    OrderResponseDTO getOrderById(Long id, Long userId);
    OrderResponseDTO getOrderByNumber(String orderNumber, Long userId);
    PageResponseDTO<OrderSummaryResponseDTO> getUserOrders(Long userId, Pageable pageable);
    PageResponseDTO<OrderSummaryResponseDTO> getOrdersByStatus(String status, Pageable pageable);
    OrderResponseDTO confirmOrder(Long id, Long userId);
    OrderResponseDTO cancelOrder(Long id, Long userId);
    OrderResponseDTO recalculateOrderWithExtras(Long orderId, List<String> newExtraCodes, Long userId);
}