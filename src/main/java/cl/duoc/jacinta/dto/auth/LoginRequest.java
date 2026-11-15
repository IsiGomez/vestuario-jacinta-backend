package cl.duoc.jacinta.dto.auth;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank @Email @Size(max = 100, message = "El correo admite hasta 100 caracteres") String correo,
        @NotBlank @Size(max = 24, message = "La contraseña admite hasta 24 caracteres") String contrasena
) {}
