package co.edu.unbosque.quickcourier.controller;

import co.edu.unbosque.quickcourier.dto.request.UpdateUserRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.MessageResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.UserResponseDTO;
import co.edu.unbosque.quickcourier.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de usuarios
 * Solo accesible por ADMIN excepto /me
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Gestión de usuarios del sistema")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del usuario actual",
            description = "Retorna la información del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserResponseDTO> getCurrentUser(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserResponseDTO user = userService.getCurrentUser(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario por ID",
            description = "Retorna la información de un usuario específico (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios",
            description = "Retorna lista paginada de usuarios (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @Parameter(description = "Configuración de paginación")
            Pageable pageable) {
        Page<UserResponseDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar usuario",
            description = "Actualiza la información de un usuario (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequestDTO request) {
        UserResponseDTO updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar usuario",
            description = "Desactiva un usuario (soft delete) (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario desactivado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    public ResponseEntity<MessageResponseDTO> deactivateUser(
            @Parameter(description = "ID del usuario", required = true)
            @PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(new MessageResponseDTO("Usuario desactivado exitosamente"));
    }
}