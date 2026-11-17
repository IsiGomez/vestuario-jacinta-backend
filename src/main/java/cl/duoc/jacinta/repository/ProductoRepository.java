package cl.duoc.jacinta.repository;

import cl.duoc.jacinta.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrue();

    @Query("select p from Producto p " +
           "where p.activo = true and p.stock <= p.stockCritico " +
           "order by p.stock asc, p.id asc")
    List<Producto> findCriticos();

    boolean existsByGeneroAndTipo(String genero, String tipo);

    boolean existsByGeneroAndTipoAndIdNot(String genero, String tipo, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);
}