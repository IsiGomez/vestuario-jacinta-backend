package cl.duoc.jacinta.repository;

import cl.duoc.jacinta.model.Boleta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

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
}