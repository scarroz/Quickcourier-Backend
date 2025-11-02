package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAddressRequestDTO(
        @NotBlank(message = "Dirección línea 1 es obligatoria")
        @Size(max = 255)
        String addressLine1,

        @Size(max = 255)
        String addressLine2,

        @NotBlank(message = "Ciudad es obligatoria")
        @Size(max = 100)
        String city,

        @NotBlank(message = "Zona es obligatoria")
        @Size(max = 50)
        String zone,

        @Size(max = 20)
        String postalCode,

        Boolean isDefault
) {}
