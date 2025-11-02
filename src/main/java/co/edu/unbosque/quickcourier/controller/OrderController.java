package co.edu.unbosque.quickcourier.controller;

import co.edu.unbosque.quickcourier.dto.request.CreateOrderRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.MessageResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.OrderResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.OrderSummaryResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.PageResponseDTO;
import co.edu.unbosque.quickcourier.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de pedidos
 * Incluye creación, consulta, confirmación y cancelación
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Gestión de pedidos con cálculo dinámico de envío y extras")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Crear nuevo pedido",
            description = "Crea un pedido con productos y extras. Calcula automáticamente el envío usando Strategy Pattern y aplica extras con Decorator Pattern")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o stock insuficiente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<OrderResponseDTO> createOrder(
            @Valid @RequestBody CreateOrderRequestDTO request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        OrderResponseDTO order = orderService.createOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Listar mis pedidos",
            description = "Obtiene el historial de pedidos del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pedidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<PageResponseDTO<OrderSummaryResponseDTO>> getMyOrders(
            @Parameter(description = "Configuración de paginación")
            Pageable pageable,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        PageResponseDTO<OrderSummaryResponseDTO> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pedido por ID",
            description = "Retorna los detalles completos de un pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "403", description = "Pedido no pertenece al usuario"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderResponseDTO order = orderService.getOrderById(id, userId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Obtener pedido por número",
            description = "Busca un pedido por su número único (ej: QC-2025-001234)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "403", description = "Pedido no pertenece al usuario"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<OrderResponseDTO> getOrderByNumber(
            @Parameter(description = "Número del pedido", required = true, example = "QC-2025-001234")
            @PathVariable String orderNumber,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderResponseDTO order = orderService.getOrderByNumber(orderNumber, userId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar pedidos por estado",
            description = "Obtiene pedidos filtrados por estado (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pedidos"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<PageResponseDTO<OrderSummaryResponseDTO>> getOrdersByStatus(
            @Parameter(description = "Estado del pedido", required = true, example = "PENDING")
            @PathVariable String status,
            @Parameter(description = "Configuración de paginación")
            Pageable pageable) {
        PageResponseDTO<OrderSummaryResponseDTO> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{id}/confirm")
    @Operation(summary = "Confirmar pedido",
            description = "Confirma un pedido para que pueda ser procesado y pagado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido confirmado"),
            @ApiResponse(responseCode = "400", description = "El pedido no puede ser confirmado en su estado actual"),
            @ApiResponse(responseCode = "403", description = "Pedido no pertenece al usuario"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<OrderResponseDTO> confirmOrder(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderResponseDTO order = orderService.confirmOrder(id, userId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar pedido",
            description = "Cancela un pedido y restaura el stock de productos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido cancelado"),
            @ApiResponse(responseCode = "400", description = "El pedido no puede ser cancelado en su estado actual"),
            @ApiResponse(responseCode = "403", description = "Pedido no pertenece al usuario"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<OrderResponseDTO> cancelOrder(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderResponseDTO order = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/recalculate-extras")
    @Operation(summary = "Recalcular pedido con nuevos extras",
            description = "Actualiza los extras de un pedido y recalcula totales (solo si no ha sido confirmado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido recalculado"),
            @ApiResponse(responseCode = "400", description = "El pedido no puede ser modificado en su estado actual"),
            @ApiResponse(responseCode = "403", description = "Pedido no pertenece al usuario"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<OrderResponseDTO> recalculateOrderWithExtras(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable Long id,
            @Parameter(description = "Códigos de extras a aplicar", example = "[\"EXPRESS\", \"INSURANCE\"]")
            @RequestBody List<String> newExtraCodes,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        OrderResponseDTO order = orderService.recalculateOrderWithExtras(id, newExtraCodes, userId);
        return ResponseEntity.ok(order);
    }
}