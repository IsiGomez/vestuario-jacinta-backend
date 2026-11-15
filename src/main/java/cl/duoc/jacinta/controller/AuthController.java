package cl.duoc.jacinta.controller;

import cl.duoc.jacinta.dto.auth.*;
import org.springframework.security.core.Authentication;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import cl.duoc.jacinta.dto.usuario.UsuarioResponse;
import cl.duoc.jacinta.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(datos));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest d) {
        return authService.login(d);
    }

    @PostMapping("/refresh")
    public AccessTokenResponse refresh(@Valid @RequestBody RefreshRequest d) {
        return authService.refresh(d);
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public UsuarioResponse me(Authentication a) {
        return authService.me(Long.valueOf(a.getName()));
    }
}
