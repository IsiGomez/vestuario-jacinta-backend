package cl.duoc.jacinta.dto.orden;

import cl.duoc.jacinta.model.EstadoBoleta;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenResponse(
        Long id,
        String numero,
        LocalDateTime fecha,
        Long usuarioId,
        EstadoBoleta estado,
        Cliente cliente,
        Entrega entrega,
        List<Item> items,
        Integer total
) {
    public record Cliente(String nombre, String apellidos, String correo) {}

    public record Entrega(String calle, String departamento, String region, String comuna, String indicaciones) {}

    public record Item(Long productoId, String nombre, Integer precio, Integer cantidad, String talla, String color) {}
}