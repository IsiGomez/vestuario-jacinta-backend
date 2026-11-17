package cl.duoc.jacinta.repository;

import cl.duoc.jacinta.model.Boleta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    @EntityGraph(attributePaths = "detalles")
    Optional<Boleta> findWithDetallesById(Long id);

    @EntityGraph(attributePaths = "detalles")
    List<Boleta> findAllByOrderByFechaDesc();

    @EntityGraph(attributePaths = "detalles")
    List<Boleta> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    long countByNumeroStartingWith(String prefijo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Boleta b where b.id = :id")
    Optional<Boleta> findByIdForUpdate(@Param("id") Long id);
}