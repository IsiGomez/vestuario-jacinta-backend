package cl.duoc.jacinta.dto.orden;

import cl.duoc.jacinta.model.EstadoBoleta;
import jakarta.validation.constraints.NotNull;

public record EstadoOrdenRequest(@NotNull(message = "El estado es obligatorio") EstadoBoleta estado) {

}