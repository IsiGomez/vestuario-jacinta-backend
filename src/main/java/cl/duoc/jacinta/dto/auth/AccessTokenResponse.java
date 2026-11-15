package cl.duoc.jacinta.dto.auth;
public record AccessTokenResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {}
