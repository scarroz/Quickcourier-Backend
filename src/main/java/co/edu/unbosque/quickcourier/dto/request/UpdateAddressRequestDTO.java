package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateAddressRequestDTO(
        @Size(max = 255)
        String addressLine1,

        @Size(max = 255)
        String addressLine2,

        @Size(max = 100)
        String city,

        @Size(max = 50)
        String zone,

        @Size(max = 20)
        String postalCode,

        Boolean isDefault
) {}
