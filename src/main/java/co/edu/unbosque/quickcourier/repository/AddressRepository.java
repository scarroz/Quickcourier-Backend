package co.edu.unbosque.quickcourier.repository;

import co.edu.unbosque.quickcourier.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY a.isDefault DESC, a.createdAt DESC")
    Page<Address> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY a.isDefault DESC, a.createdAt DESC")
    List<Address> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<Address> findDefaultByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM Address a WHERE a.zone = :zone")
    Page<Address> findByZone(@Param("zone") String zone, Pageable pageable);
}