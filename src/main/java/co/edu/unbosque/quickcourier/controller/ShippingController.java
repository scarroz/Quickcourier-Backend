package co.edu.unbosque.quickcourier.controller;

import co.edu.unbosque.quickcourier.dto.response.ShippingExtraResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.ShippingRuleResponseDTO;
import co.edu.unbosque.quickcourier.service.ShippingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para consulta de reglas de envío y extras
 * Permite ver las configuraciones de cálculo de envío (Strategy Pattern)
 */
@RestController
@RequestMapping("/api/shipping")
@Tag(name = "Shipping", description = "Reglas de envío y extras disponibles")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @GetMapping("/extras")
    @Operation(summary = "Listar extras de envío disponibles",
            description = "Obtiene todos los extras activos que se pueden agregar a un pedido (público)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de extras disponibles")
    })
    public ResponseEntity<List<ShippingExtraResponseDTO>> getActiveShippingExtras() {
        List<ShippingExtraResponseDTO> extras = shippingService.getActiveShippingExtras();
        return ResponseEntity.ok(extras);
    }

    @GetMapping("/extras/{code}")
    @Operation(summary = "Obtener extra por código",
            description = "Retorna información detallada de un extra específico (público)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Extra encontrado"),
            @ApiResponse(responseCode = "404", description = "Extra no encontrado")
    })
    public ResponseEntity<ShippingExtraResponseDTO> getShippingExtraByCode(
            @Parameter(description = "Código del extra", required = true, example = "EXPRESS")
            @PathVariable String code) {
        ShippingExtraResponseDTO extra = shippingService.getShippingExtraByCode(code);
        return ResponseEntity.ok(extra);
    }

    @GetMapping("/rules")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Listar reglas de envío activas",
            description = "Obtiene todas las reglas de cálculo de envío configuradas (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reglas activas"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<List<ShippingRuleResponseDTO>> getActiveShippingRules() {
        List<ShippingRuleResponseDTO> rules = shippingService.getActiveShippingRules();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/rules/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Obtener regla por código",
            description = "Retorna configuración detallada de una regla de envío (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Regla encontrada"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Regla no encontrada")
    })
    public ResponseEntity<ShippingRuleResponseDTO> getShippingRuleByCode(
            @Parameter(description = "Código de la regla", required = true, example = "WEEKEND_PROMO")
            @PathVariable String code) {
        ShippingRuleResponseDTO rule = shippingService.getShippingRuleByCode(code);
        return ResponseEntity.ok(rule);
    }
}