package co.edu.unbosque.quickcourier.controller;

import co.edu.unbosque.quickcourier.dto.request.CreatePaymentRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.PaymentResponseDTO;
import co.edu.unbosque.quickcourier.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para procesamiento de pagos
 * Simula integración con gateway de pago
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Procesamiento y gestión de pagos")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @Operation(summary = "Crear pago para un pedido",
            description = "Inicia un pago para un pedido confirmado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pago creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Pedido no confirmado o ya pagado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Pedido no pertenece al usuario")
    })
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @Valid @RequestBody CreatePaymentRequestDTO request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PaymentResponseDTO payment = paymentService.createPayment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pago por ID",
            description = "Retorna los detalles de un pago específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago encontrado"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    public ResponseEntity<PaymentResponseDTO> getPaymentById(
            @Parameter(description = "ID del pago", required = true)
            @PathVariable Long id) {
        PaymentResponseDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Listar pagos de un pedido",
            description = "Obtiene todos los pagos asociados a un pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pagos"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado")
    })
    public ResponseEntity<List<PaymentResponseDTO>> getOrderPayments(
            @Parameter(description = "ID del pedido", required = true)
            @PathVariable Long orderId) {
        List<PaymentResponseDTO> payments = paymentService.getOrderPayments(orderId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "Procesar pago",
            description = "Procesa un pago pendiente (simula integración con gateway)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago procesado"),
            @ApiResponse(responseCode = "400", description = "Pago no está pendiente"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    public ResponseEntity<PaymentResponseDTO> processPayment(
            @Parameter(description = "ID del pago", required = true)
            @PathVariable Long id) {
        PaymentResponseDTO payment = paymentService.processPayment(id);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reembolsar pago",
            description = "Reembolsa un pago completado (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago reembolsado"),
            @ApiResponse(responseCode = "400", description = "Pago no está completado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Pago no encontrado")
    })
    public ResponseEntity<PaymentResponseDTO> refundPayment(
            @Parameter(description = "ID del pago", required = true)
            @PathVariable Long id) {
        PaymentResponseDTO payment = paymentService.refundPayment(id);
        return ResponseEntity.ok(payment);
    }
}