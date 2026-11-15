package cl.duoc.jacinta.dto.categoria;

import jakarta.validation.constraints.*;

public record CategoriaRequest(
        @NotBlank @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres") String nombre,
        @Size(max = 500, message = "La descripción admite hasta 500 caracteres") String descripcion
) {}
