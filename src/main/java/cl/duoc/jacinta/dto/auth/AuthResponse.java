package cl.duoc.jacinta.dto.auth;

import cl.duoc.jacinta.dto.usuario.UsuarioResponse;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UsuarioResponse usuario
) {}
