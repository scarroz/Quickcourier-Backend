package co.edu.unbosque.quickcourier.service;

import co.edu.unbosque.quickcourier.dto.request.CreateProductRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.UpdateProductRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponseDTO createProduct(CreateProductRequestDTO request);
    ProductResponseDTO updateProduct(Long id, UpdateProductRequestDTO request);
    void deleteProduct(Long id);
    ProductResponseDTO getProductById(Long id);
    ProductResponseDTO getProductBySku(String sku);
    Page<ProductResponseDTO> getAllActiveProducts(Pageable pageable);
    Page<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable);
    Page<ProductResponseDTO> searchProducts(String searchTerm, Pageable pageable);
    Page<ProductResponseDTO> getLowStockProducts(Integer threshold, Pageable pageable);
}