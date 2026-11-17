package cl.duoc.jacinta.dto.producto;

import jakarta.validation.constraints.*;
import java.util.List;

public record ProductoRequest(
        @NotBlank @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
        String nombre,

        @NotBlank @Pattern(regexp = "hombre|mujer|unisex", message = "El género debe ser hombre, mujer o unisex")
        String genero,

        @NotBlank @Size(max = 50, message = "El tipo admite hasta 50 caracteres")
        String tipo,

        @NotNull(message = "La categoría es obligatoria")
        Long categoriaId,

        @NotNull @PositiveOrZero(message = "El precio no puede ser negativo")
        Integer precio,

        @NotNull @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull @PositiveOrZero(message = "El stock crítico no puede ser negativo")
        Integer stockCritico,

        @Size(max = 500, message = "La descripción admite hasta 500 caracteres")
        String descripcion,

        @NotEmpty(message = "Indica al menos una talla")
        List<@NotBlank String> tallas,

        @NotEmpty(message = "Indica al menos un color")
        List<@NotBlank String> colores,

        Boolean activo
) {

}