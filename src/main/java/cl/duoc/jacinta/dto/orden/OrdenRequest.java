package cl.duoc.jacinta.dto.orden;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record OrdenRequest(
        @NotNull(message = "Los datos del cliente son obligatorios")
        @Valid Cliente cliente,

        @NotNull(message = "Los datos de entrega son obligatorios")
        @Valid Entrega entrega,

        @NotEmpty(message = "La orden debe incluir al menos un producto")
        List<@NotNull @Valid Item> items
) {
    public record Cliente(
            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 50, message = "El nombre admite hasta 50 caracteres")
            String nombre,

            @NotNull(message = "Los apellidos son obligatorios")
            @Size(max = 100, message = "Los apellidos admiten hasta 100 caracteres")
            String apellidos,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no es válido")
            @Size(max = 100, message = "El correo admite hasta 100 caracteres")
            String correo
    ) {}

    public record Entrega(
            @NotBlank(message = "La dirección es obligatoria")
            @Size(max = 300, message = "La dirección admite hasta 300 caracteres")
            String calle,

            @Size(max = 50, message = "El departamento admite hasta 50 caracteres")
            String departamento,

            @NotNull(message = "La región es obligatoria")
            @Size(max = 100, message = "La región admite hasta 100 caracteres")
            String region,

            @NotNull(message = "La comuna es obligatoria")
            @Size(max = 100, message = "La comuna admite hasta 100 caracteres")
            String comuna,

            @Size(max = 500, message = "Las indicaciones admiten hasta 500 caracteres")
            String indicaciones
    ) {}

    public record Item(
            @NotNull(message = "El producto es obligatorio")
            Long productoId,

            @NotNull(message = "La cantidad es obligatoria")
            @Min(value = 1, message = "La cantidad mínima es 1")
            Integer cantidad,

            @NotBlank(message = "La talla es obligatoria")
            String talla,

            @NotBlank(message = "El color es obligatorio")
            String color
    ) {}
}