package co.edu.unbosque.quickcourier.controller;

import co.edu.unbosque.quickcourier.dto.request.CreateCategoryRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.UpdateCategoryRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.CategoryResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.MessageResponseDTO;
import co.edu.unbosque.quickcourier.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de categorías de productos
 * Lectura pública, escritura solo ADMIN
 */
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Gestión de categorías de productos")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las categorías activas",
            description = "Obtiene lista de categorías activas (público)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de categorías")
    })
    public ResponseEntity<List<CategoryResponseDTO>> getAllActiveCategories() {
        List<CategoryResponseDTO> categories = categoryService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Listar todas las categorías (incluye inactivas)",
            description = "Obtiene lista paginada de todas las categorías (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista paginada de categorías"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<Page<CategoryResponseDTO>> getAllCategories(
            @Parameter(description = "Configuración de paginación")
            Pageable pageable) {
        Page<CategoryResponseDTO> categories = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID",
            description = "Retorna una categoría específica (público)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
            @Parameter(description = "ID de la categoría", required = true)
            @PathVariable Long id) {
        CategoryResponseDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Crear nueva categoría",
            description = "Crea una categoría de productos (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o categoría duplicada"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<CategoryResponseDTO> createCategory(
            @Valid @RequestBody CreateCategoryRequestDTO request) {
        CategoryResponseDTO category = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Actualizar categoría",
            description = "Actualiza una categoría existente (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @Parameter(description = "ID de la categoría", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequestDTO request) {
        CategoryResponseDTO category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Eliminar categoría",
            description = "Desactiva una categoría (soft delete) (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría eliminada"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<MessageResponseDTO> deleteCategory(
            @Parameter(description = "ID de la categoría", required = true)
            @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new MessageResponseDTO("Categoría eliminada exitosamente"));
    }
}