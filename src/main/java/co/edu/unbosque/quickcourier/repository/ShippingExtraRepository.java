package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.ShippingExtra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingExtraRepository extends JpaRepository<ShippingExtra, Long> {

    Optional<ShippingExtra> findByCode(String code);

    @Query("SELECT se FROM ShippingExtra se WHERE se.isActive = true ORDER BY se.displayOrder, se.name")
    List<ShippingExtra> findAllActiveOrderedByDisplay();

    @Query("SELECT se FROM ShippingExtra se ORDER BY se.displayOrder, se.name")
    Page<ShippingExtra> findAllOrderedByDisplay(Pageable pageable);

    @Query("SELECT se FROM ShippingExtra se WHERE se.code IN :codes AND se.isActive = true")
    List<ShippingExtra> findByCodesAndActive(@Param("codes") List<String> codes);

    boolean existsByCode(String code);
}
