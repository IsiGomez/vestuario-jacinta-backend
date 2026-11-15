package cl.duoc.jacinta.dto.usuario;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import cl.duoc.jacinta.model.TipoUsuario;

public record UsuarioRequest(
    @NotBlank @Size(max = 12, message = "El RUN admite hasta 12 caracteres") String run,
    @NotBlank @Size(max = 50, message = "El nombre admite hasta 50 caracteres") String nombre,
    @NotBlank @Size(max = 100, message = "Los apellidos admiten hasta 100 caracteres") String apellidos,
    @NotBlank @Email @Size(max = 100, message = "El correo admite hasta 100 caracteres") String correo,
    @Size(max = 24, message = "La contraseña admite hasta 24 caracteres") String contrasena,
    @NotBlank @Size(max = 25, message = "El teléfono admite hasta 25 caracteres") String telefono,
    @Past LocalDate fechaNacimiento,
    @NotBlank @Size(max = 100, message = "La región admite hasta 100 caracteres") String region,
    @NotBlank @Size(max = 100, message = "La comuna admite hasta 100 caracteres") String comuna,
    @NotBlank @Size(max = 300, message = "La dirección admite hasta 300 caracteres") String direccion,
    TipoUsuario tipoUsuario
) {}
