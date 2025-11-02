package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequestDTO(
        @NotBlank(message = "Nombre es requerido")
        @Size(max = 100)
        String name,

        String description,

        Boolean isActive
) {}