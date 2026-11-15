package cl.duoc.jacinta.repository;

import cl.duoc.jacinta.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombre(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    @Query(value = "SELECT COUNT(*) FROM producto WHERE categoria_id = :id", nativeQuery = true)
    long contarProductos(Long id);
}