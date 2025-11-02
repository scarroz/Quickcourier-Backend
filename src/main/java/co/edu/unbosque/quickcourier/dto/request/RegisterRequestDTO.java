package co.edu.unbosque.quickcourier.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "Email es obligatorio")
        @Email(message = "Email debe ser válido")
        String email,

        @NotBlank(message = "Password es obligatorio")
        @Size(min = 6, max = 50, message = "Password debe tener entre 6 y 50 caracteres")
        String password,

        @NotBlank(message = "Nombre es obligatorio")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Apellido es obligatorio")
        @Size(max = 100)
        String lastName,

        @Pattern(regexp = "^[0-9]{10}$", message = "Teléfono debe tener 10 dígitos")
        String phone
) {}
