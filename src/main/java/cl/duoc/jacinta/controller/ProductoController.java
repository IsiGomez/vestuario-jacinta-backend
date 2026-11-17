package cl.duoc.jacinta.controller;

import cl.duoc.jacinta.dto.producto.*;
import cl.duoc.jacinta.service.ProductoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {
    private final ProductoService productos;

    private static boolean esStaff(Authentication a) {
        return a != null && a.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_ADMINISTRADOR") || r.equals("ROLE_VENDEDOR"));
    }

    @GetMapping
    public List<ProductoResponse> listar(Authentication a) {
        return productos.listar(esStaff(a));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/criticos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public List<ProductoResponse> criticos() {
        return productos.criticos();
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id, Authentication a) {
        return productos.obtener(id, esStaff(a));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest d) {
        return ResponseEntity.status(201).body(productos.guardar(null, d));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public ProductoResponse editar(@PathVariable Long id, @Valid @RequestBody ProductoRequest d) {
        return productos.guardar(id, d);
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        productos.eliminar(id);
    }
}