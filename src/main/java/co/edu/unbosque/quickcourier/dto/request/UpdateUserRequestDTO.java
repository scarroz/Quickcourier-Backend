package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateUserRequestDTO(
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        @Size(max = 20)
        String phone,

        Boolean isActive
) {}
