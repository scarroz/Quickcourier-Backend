package co.edu.unbosque.quickcourier.service;

import co.edu.unbosque.quickcourier.dto.request.CreateCategoryRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.UpdateCategoryRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.CategoryResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    CategoryResponseDTO createCategory(CreateCategoryRequestDTO request);
    CategoryResponseDTO updateCategory(Long id, UpdateCategoryRequestDTO request);
    void deleteCategory(Long id);
    CategoryResponseDTO getCategoryById(Long id);
    List<CategoryResponseDTO> getAllActiveCategories();
    Page<CategoryResponseDTO> getAllCategories(Pageable pageable);
}
