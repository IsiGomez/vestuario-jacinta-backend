package cl.duoc.jacinta.dto.producto;

import java.util.List;

public record ProductoResponse(
        Long id,
        String nombre,
        String genero,
        String tipo,
        Long categoriaId,
        Integer precio,
        Integer stock,
        Integer stockCritico,
        String descripcion,
        List<String> tallas,
        List<String> colores,
        Boolean activo
) {

}