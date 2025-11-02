package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT al FROM AuditLog al WHERE al.user.id = :userId ORDER BY al.createdAt DESC")
    Page<AuditLog> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE " +
            "al.entityType = :entityType AND al.entityId = :entityId " +
            "ORDER BY al.createdAt DESC")
    Page<AuditLog> findByEntity(
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            Pageable pageable
    );

    @Query("SELECT al FROM AuditLog al WHERE al.action = :action ORDER BY al.createdAt DESC")
    Page<AuditLog> findByAction(@Param("action") String action, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startDate AND :endDate ORDER BY al.createdAt DESC")
    Page<AuditLog> findBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}