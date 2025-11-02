package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.Payment;
import co.edu.unbosque.quickcourier.model.PaymentStatus;
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
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(String transactionId);

    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId ORDER BY p.createdAt DESC")
    List<Payment> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT p FROM Payment p WHERE p.status = :status ORDER BY p.createdAt DESC")
    Page<Payment> findByStatus(@Param("status") PaymentStatus status, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    Page<Payment> findBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}