package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.name")
    Page<Product> findAllActive(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true ORDER BY p.name")
    Page<Product> findByCategoryIdAndActive(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "AND p.isActive = true ORDER BY p.name")
    Page<Product> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold AND p.isActive = true ORDER BY p.stockQuantity ASC")
    Page<Product> findLowStockProducts(@Param("threshold") Integer threshold, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isActive = true ORDER BY p.price ASC")
    Page<Product> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice,
                                   @Param("maxPrice") java.math.BigDecimal maxPrice,
                                   Pageable pageable);

    boolean existsBySku(String sku);
    @Query("""
        SELECT p FROM Product p
        WHERE p.isActive = true
          AND (
              LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
              OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
              OR LOWER(p.category.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
              OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
          )
        ORDER BY p.name ASC
        """)
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);
    @Query("""
        SELECT p FROM Product p
        WHERE p.category.id = :categoryId
          AND p.isActive = true
        ORDER BY p.name ASC
        """)
    Page<Product> findByCategoryAndActive(@Param("categoryId") Long categoryId, Pageable pageable);

}