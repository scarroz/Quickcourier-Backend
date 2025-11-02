package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ApplyShippingRuleRequestDTO  (
        @NotBlank(message = "Código de regla es obligatorio")
        String ruleCode
) {}
