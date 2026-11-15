package cl.duoc.jacinta.dto.auth;

import jakarta.validation.constraints.*;

public record RefreshRequest(
    @NotBlank @Size(max = 4096, message = "El token admite hasta 4096 caracteres") String refreshToken
) {}
