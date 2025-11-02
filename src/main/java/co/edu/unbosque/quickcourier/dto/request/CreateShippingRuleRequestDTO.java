package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateShippingRuleRequestDTO(
        @NotBlank(message = "Código es obligatorio")
        @Size(max = 50)
        String code,

        @NotBlank(message = "Nombre es obligatorio")
        @Size(max = 100)
        String name,

        String description,

        @NotBlank(message = "Tipo de regla es obligatorio")
        String ruleType,

        @NotNull(message = "Prioridad es obligatoria")
        Integer priority,

        @NotNull(message = "Configuración es obligatoria")
        Map<String, Object> configuration
) {}
