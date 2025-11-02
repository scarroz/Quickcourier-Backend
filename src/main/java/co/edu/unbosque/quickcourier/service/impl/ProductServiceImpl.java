package co.edu.unbosque.quickcourier.service.impl;

import co.edu.unbosque.quickcourier.dto.request.CreateProductRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.UpdateProductRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.ProductResponseDTO;
import co.edu.unbosque.quickcourier.exception.BadRequestException;
import co.edu.unbosque.quickcourier.exception.ResourceNotFoundException;
import co.edu.unbosque.quickcourier.mapper.DataMapper;
import co.edu.unbosque.quickcourier.model.Category;
import co.edu.unbosque.quickcourier.model.Product;
import co.edu.unbosque.quickcourier.repository.CategoryRepository;
import co.edu.unbosque.quickcourier.repository.ProductRepository;
import co.edu.unbosque.quickcourier.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementación del servicio de productos con caché para performance
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final DataMapper dataMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              DataMapper dataMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.dataMapper = dataMapper;
    }

    @Override
    @CacheEvict(value = {"products", "productsBySku"}, allEntries = true)
    public ProductResponseDTO createProduct(CreateProductRequestDTO request) {
        logger.info("Creating product: {}", request.name());

        // Validar SKU duplicado
        if (productRepository.existsBySku(request.sku())) {
            throw new BadRequestException("Ya existe un producto con ese SKU");
        }

        // Validar categoría
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        // Crear producto
        Product product = new Product();
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategory(category);
        product.setPrice(request.price());
        product.setWeightKg(request.weightKg());
        product.setStockQuantity(request.stockQuantity() != null ? request.stockQuantity() : 0);
        product.setImageUrl(request.imageUrl());

        // 🔹 Por defecto todos los productos nuevos están activos
        product.setIsActive(true);

        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        logger.info("Product created: {} (SKU: {})", savedProduct.getName(), savedProduct.getSku());

        return dataMapper.toProductResponseDTO(savedProduct);
    }


    @Override
    @CacheEvict(value = {"products", "productsBySku"}, key = "#id")
    public ProductResponseDTO updateProduct(Long id, UpdateProductRequestDTO request) {
        logger.info("Updating product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
            product.setCategory(category);
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.weightKg() != null) {
            product.setWeightKg(request.weightKg());
        }
        if (request.stockQuantity() != null) {
            product.setStockQuantity(request.stockQuantity());
        }
        if (request.isActive() != null) {
            product.setIsActive(request.isActive());
        }
        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl());
        }

        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        logger.info("Product {} updated", id);

        return dataMapper.toProductResponseDTO(updatedProduct);
    }

    @Override
    @CacheEvict(value = {"products", "productsBySku"}, key = "#id")
    public void deleteProduct(Long id) {
        logger.info("Deleting product: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        // Soft delete
        product.setIsActive(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        logger.info("Product {} deactivated", id);
    }

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductResponseDTO getProductById(Long id) {
        logger.debug("Fetching product by id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        return dataMapper.toProductResponseDTO(product);
    }

    @Override
    @Cacheable(value = "productsBySku", key = "#sku")
    public ProductResponseDTO getProductBySku(String sku) {
        logger.debug("Fetching product by sku: {}", sku);

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        return dataMapper.toProductResponseDTO(product);
    }

    @Override
    @Cacheable(value = "products", key = "'active_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductResponseDTO> getAllActiveProducts(Pageable pageable) {
        logger.debug("Fetching active products - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Product> products = productRepository.findAllActive(pageable);

        return products.map(dataMapper::toProductResponseDTO);
    }

    @Override
    @Cacheable(value = "products", key = "'category_' + #categoryId + '_' + #pageable.pageNumber")
    public Page<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        logger.debug("Fetching products by category: {}", categoryId);

        // Verificar que la categoría existe
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Categoría no encontrada");
        }

        Page<Product> products = productRepository.findByCategoryAndActive(categoryId, pageable);

        return products.map(dataMapper::toProductResponseDTO);
    }

    @Override
    public Page<ProductResponseDTO> searchProducts(String searchTerm, Pageable pageable) {
        logger.debug("Searching products with term: {}", searchTerm);

        Page<Product> products = productRepository.searchProducts(searchTerm, pageable);

        return products.map(dataMapper::toProductResponseDTO);
    }

    @Override
    public Page<ProductResponseDTO> getLowStockProducts(Integer threshold, Pageable pageable) {
        logger.debug("Fetching low stock products (threshold: {})", threshold);

        Integer actualThreshold = threshold != null ? threshold : 10;

        Page<Product> products = productRepository.findLowStockProducts(actualThreshold, pageable);

        return products.map(dataMapper::toProductResponseDTO);
    }
}