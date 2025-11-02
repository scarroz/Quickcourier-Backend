package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.TokenBlackList;
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
public interface TokenBlackListRepository extends JpaRepository<TokenBlackList, Long> {

    boolean existsByJti(String jti);

    Optional<TokenBlackList> findByJti(String jti);


    boolean existsByJtiAndUserId(String jti, Long userId);

    @Modifying
    @Query("DELETE FROM TokenBlackList tb WHERE tb.expiresAt < :date")
    void deleteExpiredTokens(@Param("date") LocalDateTime date);

    @Query("SELECT tb FROM TokenBlackList tb WHERE tb.user.id = :userId ORDER BY tb.revokedAt DESC")
    Page<TokenBlackList> findByUserId(@Param("userId") Long userId, Pageable pageable);
}