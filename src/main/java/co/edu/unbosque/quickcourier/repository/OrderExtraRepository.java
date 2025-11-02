package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.OrderExtra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderExtraRepository extends JpaRepository<OrderExtra, Long> {

    @Query("SELECT oe FROM OrderExtra oe WHERE oe.order.id = :orderId")
    List<OrderExtra> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT oe FROM OrderExtra oe WHERE oe.shippingExtra.id = :extraId")
    Page<OrderExtra> findByShippingExtraId(@Param("extraId") Long extraId, Pageable pageable);
}
