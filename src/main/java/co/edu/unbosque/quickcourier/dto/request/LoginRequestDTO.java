package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @NotBlank(message = "Email es obligatorio")
        @Email(message = "Email debe ser válido")
        String email,

        @NotBlank(message = "Password es obligatorio")
        @Size(min = 6, message = "Password debe tener al menos 6 caracteres")
        String password
) {}
