package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
public record CreatePaymentRequestDTO(
        @NotNull(message = "ID de orden es requerido")
        Long orderId,

        @NotBlank(message = "Método de pago es requerido")
        String paymentMethod,

        String gatewayResponse
) {}
