package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.id = :productId")
    Page<OrderItem> findByProductId(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);
}