package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.ShippingRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShippingRuleRepository extends JpaRepository<ShippingRule, Long> {

    Optional<ShippingRule> findByCode(String code);

    @Query("SELECT sr FROM ShippingRule sr WHERE sr.isActive = true " +
            "AND (sr.validFrom IS NULL OR sr.validFrom <= :currentDate) " +
            "AND (sr.validUntil IS NULL OR sr.validUntil >= :currentDate) " +
            "ORDER BY sr.priority ASC")
    List<ShippingRule> findActiveAndValidRules(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT sr FROM ShippingRule sr WHERE sr.isActive = true ORDER BY sr.priority ASC")
    List<ShippingRule> findAllActiveOrderByPriority();

    @Query("SELECT sr FROM ShippingRule sr ORDER BY sr.priority ASC")
    Page<ShippingRule> findAllOrderByPriority(Pageable pageable);

    boolean existsByCode(String code);
}
