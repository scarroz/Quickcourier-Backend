package co.edu.unbosque.quickcourier.controller;

import co.edu.unbosque.quickcourier.dto.request.CreateProductRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.UpdateProductRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.MessageResponseDTO;
import co.edu.unbosque.quickcourier.dto.response.ProductResponseDTO;
import co.edu.unbosque.quickcourier.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de productos
 * Lectura pública, escritura solo ADMIN
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Catálogo de productos (libros, snacks, accesorios)")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Listar productos activos",
            description = "Obtiene lista paginada de productos activos (público)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de productos")
    })
    public ResponseEntity<Page<ProductResponseDTO>> getAllActiveProducts(
            @Parameter(description = "Configuración de paginación")
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getAllActiveProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID",
            description = "Retorna un producto específico (público)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductResponseDTO> getProductById(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Obtener producto por SKU",
            description = "Busca un producto por su código SKU (público)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductResponseDTO> getProductBySku(
            @Parameter(description = "SKU del producto", required = true, example = "BOOK-001")
            @PathVariable String sku) {
        ProductResponseDTO product = productService.getProductBySku(sku);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Listar productos por categoría",
            description = "Obtiene productos de una categoría específica (público)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de productos"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByCategory(
            @Parameter(description = "ID de la categoría", required = true)
            @PathVariable Long categoryId,
            @Parameter(description = "Configuración de paginación")
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar productos",
            description = "Busca productos por nombre o descripción (público)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultados de búsqueda")
    })
    public ResponseEntity<Page<ProductResponseDTO>> searchProducts(
            @Parameter(description = "Término de búsqueda", required = true, example = "Harry Potter")
            @RequestParam String q,
            @Parameter(description = "Configuración de paginación")
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.searchProducts(q, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Productos con stock bajo",
            description = "Lista productos con stock menor o igual al umbral (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de productos con stock bajo"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<Page<ProductResponseDTO>> getLowStockProducts(
            @Parameter(description = "Umbral de stock", example = "10")
            @RequestParam(required = false, defaultValue = "10") Integer threshold,
            @Parameter(description = "Configuración de paginación")
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getLowStockProducts(threshold, pageable);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Crear nuevo producto",
            description = "Crea un producto en el catálogo (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o SKU duplicado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Valid @RequestBody CreateProductRequestDTO request) {
        ProductResponseDTO product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Actualizar producto",
            description = "Actualiza un producto existente (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequestDTO request) {
        ProductResponseDTO product = productService.updateProduct(id, request);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Eliminar producto",
            description = "Desactiva un producto (soft delete) (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto eliminado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    public ResponseEntity<MessageResponseDTO> deleteProduct(
            @Parameter(description = "ID del producto", required = true)
            @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new MessageResponseDTO("Producto eliminado exitosamente"));
    }
}