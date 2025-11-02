package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.User;
import co.edu.unbosque.quickcourier.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUuid(UUID uuid);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    Page<User> findActiveUsersByRole(@Param("role") UserRole role, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'CUSTOMER'")
    long countCustomers();
}