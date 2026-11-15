package cl.duoc.jacinta.repository;

import cl.duoc.jacinta.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombre(String nombre);
}