package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.Order;
import co.edu.unbosque.quickcourier.model.OrderStatus;
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
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findByStatus(@Param("status") OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    Page<Order> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    @Query("SELECT o FROM Order o WHERE o.paymentStatus = :paymentStatus ORDER BY o.createdAt DESC")
    Page<Order> findByPaymentStatus(@Param("paymentStatus") PaymentStatus paymentStatus, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    Page<Order> findOrdersBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt ASC")
    Optional<Order> findFirstOrderByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END " +
            "FROM Order o WHERE o.user.id = :userId")
    boolean hasOrders(@Param("userId") Long userId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE " +
            "o.status = 'DELIVERED' AND o.paymentStatus = 'PAID' " +
            "AND o.createdAt BETWEEN :startDate AND :endDate")
    Optional<java.math.BigDecimal> calculateRevenueBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.createdAt DESC")
    Page<Order> findByStatusIn(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);
}