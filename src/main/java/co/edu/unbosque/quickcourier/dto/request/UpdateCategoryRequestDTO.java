package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateCategoryRequestDTO(
        @Size(max = 100)
        String name,

        String description,

        Boolean isActive
) {}
