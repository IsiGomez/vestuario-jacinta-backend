package cl.duoc.jacinta.controller;

import cl.duoc.jacinta.dto.orden.EstadoOrdenRequest;
import cl.duoc.jacinta.dto.orden.OrdenRequest;
import cl.duoc.jacinta.dto.orden.OrdenResponse;
import cl.duoc.jacinta.security.Sesion;
import cl.duoc.jacinta.service.OrdenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@RequiredArgsConstructor
public class OrdenController {
    private final OrdenService ordenes;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<OrdenResponse> crear(@Valid @RequestBody OrdenRequest d, Authentication a) {
        return ResponseEntity.status(201).body(ordenes.crear(d, Sesion.usuarioId(a)));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public List<OrdenResponse> listar() {
        return ordenes.listar();
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public OrdenResponse obtener(@PathVariable Long id, Authentication a) {
        return ordenes.obtener(id, Sesion.usuarioId(a), Sesion.esStaff(a));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/usuario/{usuarioId}")
    public List<OrdenResponse> porUsuario(@PathVariable Long usuarioId, Authentication a) {
        return ordenes.porUsuario(usuarioId, Sesion.usuarioId(a), Sesion.esStaff(a));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public OrdenResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoOrdenRequest d) {
        return ordenes.cambiarEstado(id, d.estado());
    }
}