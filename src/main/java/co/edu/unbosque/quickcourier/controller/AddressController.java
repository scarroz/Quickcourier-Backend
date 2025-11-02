package co.edu.unbosque.quickcourier.controller;

import co.edu.unbosque.quickcourier.dto.request.CreateAddressRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.UpdateAddressRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.AddressResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.MessageResponseDTO;
import co.edu.unbosque.quickcourier.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de direcciones de entrega
 * El usuario solo puede gestionar sus propias direcciones
 */
@RestController
@RequestMapping("/api/addresses")
@Tag(name = "Addresses", description = "Gestión de direcciones de entrega")
@SecurityRequirement(name = "Bearer Authentication")
public class AddressController {

private AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }


    @PostMapping
    @Operation(summary = "Crear nueva dirección",
            description = "Crea una dirección de entrega para el usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dirección creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<AddressResponseDTO> createAddress(
            @Valid @RequestBody CreateAddressRequestDTO request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        AddressResponseDTO address = addressService.createAddress(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }

    @GetMapping
    @Operation(summary = "Listar direcciones del usuario",
            description = "Obtiene todas las direcciones del usuario autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de direcciones"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    public ResponseEntity<List<AddressResponseDTO>> getUserAddresses(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<AddressResponseDTO> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener dirección por ID",
            description = "Retorna una dirección específica del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dirección encontrada"),
            @ApiResponse(responseCode = "403", description = "Dirección no pertenece al usuario"),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada")
    })
    public ResponseEntity<AddressResponseDTO> getAddressById(
            @Parameter(description = "ID de la dirección", required = true)
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AddressResponseDTO address = addressService.getAddressById(id, userId);
        return ResponseEntity.ok(address);
    }

    @GetMapping("/default")
    @Operation(summary = "Obtener dirección predeterminada",
            description = "Retorna la dirección marcada como predeterminada del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dirección predeterminada encontrada"),
            @ApiResponse(responseCode = "404", description = "No hay dirección predeterminada")
    })
    public ResponseEntity<AddressResponseDTO> getDefaultAddress(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AddressResponseDTO address = addressService.getDefaultAddress(userId);
        return ResponseEntity.ok(address);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar dirección",
            description = "Actualiza una dirección existente del usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dirección actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Dirección no pertenece al usuario"),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada")
    })
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @Parameter(description = "ID de la dirección", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateAddressRequestDTO request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        AddressResponseDTO address = addressService.updateAddress(id, request, userId);
        return ResponseEntity.ok(address);
    }

    @PatchMapping("/{id}/set-default")
    @Operation(summary = "Establecer como predeterminada",
            description = "Marca una dirección como predeterminada para entregas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dirección establecida como predeterminada"),
            @ApiResponse(responseCode = "403", description = "Dirección no pertenece al usuario"),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada")
    })
    public ResponseEntity<AddressResponseDTO> setDefaultAddress(
            @Parameter(description = "ID de la dirección", required = true)
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AddressResponseDTO address = addressService.setDefaultAddress(id, userId);
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar dirección",
            description = "Elimina una dirección (no se puede eliminar la única dirección)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dirección eliminada"),
            @ApiResponse(responseCode = "400", description = "No se puede eliminar la única dirección"),
            @ApiResponse(responseCode = "403", description = "Dirección no pertenece al usuario"),
            @ApiResponse(responseCode = "404", description = "Dirección no encontrada")
    })
    public ResponseEntity<MessageResponseDTO> deleteAddress(
            @Parameter(description = "ID de la dirección", required = true)
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        addressService.deleteAddress(id, userId);
        return ResponseEntity.ok(new MessageResponseDTO("Dirección eliminada exitosamente"));
    }
}