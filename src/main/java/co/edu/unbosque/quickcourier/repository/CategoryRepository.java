package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.name")
    Page<Category> findAllActive(Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.name")
    List<Category> findAllActiveList();

    boolean existsByName(String name);
}
