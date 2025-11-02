package co.edu.unbosque.quickcourier.service.impl;

import co.edu.unbosque.quickcourier.dto.request.CreateCategoryRequestDTO;
import co.edu.unbosque.quickcourier.dto.request.UpdateCategoryRequestDTO;
import co.edu.unbosque.quickcourier.dto.response.CategoryResponseDTO;
import co.edu.unbosque.quickcourier.exception.BadRequestException;
import co.edu.unbosque.quickcourier.exception.ResourceNotFoundException;
import co.edu.unbosque.quickcourier.mapper.DataMapper;
import co.edu.unbosque.quickcourier.model.Category;
import co.edu.unbosque.quickcourier.repository.CategoryRepository;
import co.edu.unbosque.quickcourier.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de categorías
 */
@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final DataMapper dataMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, DataMapper dataMapper) {
        this.categoryRepository = categoryRepository;
        this.dataMapper = dataMapper;
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDTO createCategory(CreateCategoryRequestDTO request) {
        logger.info("Creating category: {}", request.name());

        if (categoryRepository.existsByName(request.name())) {
            throw new BadRequestException("Ya existe una categoría con ese nombre");
        }

        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        category.setIsActive(request.isActive()      != null ? request.isActive() : true);
        category.setCreatedAt(LocalDateTime.now());

        Category savedCategory = categoryRepository.save(category);

        logger.info("Category created: {}", savedCategory.getName());

        return dataMapper.toCategoryResponseDTO(savedCategory);
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponseDTO updateCategory(Long id, UpdateCategoryRequestDTO request) {
        logger.info("Updating category: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        if (request.name() != null) {
            if (!request.name().equals(category.getName()) &&
                    categoryRepository.existsByName(request.name())) {
                throw new BadRequestException("Ya existe una categoría con ese nombre");
            }
            category.setName(request.name());
        }

        if (request.description() != null) {
            category.setDescription(request.description());
        }

        if (request.isActive() != null) {
            category.setIsActive(request.isActive());
        }

        Category updatedCategory = categoryRepository.save(category);

        logger.info("Category {} updated", id);

        return dataMapper.toCategoryResponseDTO(updatedCategory);
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        logger.info("Deleting category: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        // Soft delete - desactivar en lugar de eliminar
        category.setIsActive(false);
        categoryRepository.save(category);

        logger.info("Category {} deactivated", id);
    }

    @Override
    @Cacheable(value = "categories", key = "#id")
    public CategoryResponseDTO getCategoryById(Long id) {
        logger.debug("Fetching category: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        return dataMapper.toCategoryResponseDTO(category);
    }

    @Override
    @Cacheable(value = "categories", key = "'active'")
    public List<CategoryResponseDTO> getAllActiveCategories() {
        logger.debug("Fetching all active categories");

        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(dataMapper::toCategoryResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CategoryResponseDTO> getAllCategories(Pageable pageable) {
        logger.debug("Fetching all categories - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Category> categories = categoryRepository.findAll(pageable);

        return categories.map(dataMapper::toCategoryResponseDTO);
    }
}