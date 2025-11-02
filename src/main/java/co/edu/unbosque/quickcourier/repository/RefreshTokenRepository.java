package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.isRevoked = false")
    List<RefreshToken> findActiveTokensByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :date")
    void deleteExpiredTokens(@Param("date") LocalDateTime date);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true, rt.revokedAt = :revokedAt " +
            "WHERE rt.user.id = :userId AND rt.isRevoked = false")
    void revokeAllUserTokens(@Param("userId") Long userId, @Param("revokedAt") LocalDateTime revokedAt);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash AND rt.user.id = :userId")
    Optional<RefreshToken> findByTokenHashAndUserId(String tokenHash, Long userId);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true, rt.revokedAt = CURRENT_TIMESTAMP WHERE rt.user.id = :userId AND rt.isRevoked = false")
    void revokeAllByUserId(Long userId);

    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash AND rt.isRevoked = false")
    boolean existsByTokenHashAndNotRevoked(String tokenHash);
}
