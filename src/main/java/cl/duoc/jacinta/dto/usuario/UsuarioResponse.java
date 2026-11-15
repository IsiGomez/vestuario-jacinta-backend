package cl.duoc.jacinta.dto.usuario;

import cl.duoc.jacinta.model.TipoUsuario;
import java.time.LocalDate;

public record UsuarioResponse(
    Long id,
    String run,
    String nombre,
    String apellidos,
    String correo,
    String telefono,
    LocalDate fechaNacimiento,
    String region,
    String comuna,
    String direccion,
    TipoUsuario tipoUsuario
) {}
