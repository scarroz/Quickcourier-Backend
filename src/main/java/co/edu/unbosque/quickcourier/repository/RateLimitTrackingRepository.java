package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.RateLimitTracking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RateLimitTrackingRepository extends JpaRepository<RateLimitTracking, Long> {

    @Query("SELECT rlt FROM RateLimitTracking rlt WHERE " +
            "rlt.user.id = :userId AND rlt.endpoint = :endpoint " +
            "AND rlt.windowEnd > :currentTime")
    Optional<RateLimitTracking> findActiveWindow(
            @Param("userId") Long userId,
            @Param("endpoint") String endpoint,
            @Param("currentTime") LocalDateTime currentTime
    );

    @Modifying
    @Query("DELETE FROM RateLimitTracking rlt WHERE rlt.windowEnd < :date")
    void deleteOldRecords(@Param("date") LocalDateTime date);

    @Query("SELECT rlt FROM RateLimitTracking rlt WHERE rlt.user.id = :userId ORDER BY rlt.windowStart DESC")
    Page<RateLimitTracking> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
