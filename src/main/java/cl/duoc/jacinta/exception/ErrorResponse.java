package cl.duoc.jacinta.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
        int status,
        String error,
        String mensaje,
        Map<String, String> detalles,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(int status, String error, String mensaje) {
        return new ErrorResponse(status, error, mensaje, null, LocalDateTime.now());
    }

    public static ErrorResponse of(int status, String error, String mensaje, Map<String, String> detalles) {
        return new ErrorResponse(status, error, mensaje, detalles, LocalDateTime.now());
    }
}