package cl.duoc.jacinta.service;

import cl.duoc.jacinta.dto.producto.ProductoRequest;
import cl.duoc.jacinta.model.Categoria;
import cl.duoc.jacinta.model.Producto;
import cl.duoc.jacinta.repository.CategoriaRepository;
import cl.duoc.jacinta.repository.DetalleBoletaRepository;
import cl.duoc.jacinta.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductoServiceTest {
    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final CategoriaRepository categorias = mock(CategoriaRepository.class);
    private final DetalleBoletaRepository detalles = mock(DetalleBoletaRepository.class);
    private final ProductoService servicio = new ProductoService(productos, categorias, detalles);

    private Categoria categoria() {
        Categoria c = new Categoria();
        c.setId(1L);
        c.setNombre("Camisas");
        return c;
    }

    private ProductoRequest solicitud() {
        return new ProductoRequest("  Camisa manga corta hombre ", "Hombre", "cam_corta", 1L, 14990, 25, 5,
                "Camisa cómoda", List.of("S", " M "), List.of("Azul", "azul", "rojo"), null);
    }

    private Producto producto(long id, boolean activo) {
        Producto p = new Producto();
        p.setId(id);
        p.setCategoria(categoria());
        p.setActivo(activo);
        return p;
    }

    private int estado(ResponseStatusException e) {
        return e.getStatusCode().value();
    }

    @Test
    void creaProductoNormalizandoDatos() {
        when(categorias.findById(1L)).thenReturn(Optional.of(categoria()));
        when(productos.saveAndFlush(any(Producto.class))).thenAnswer(a -> a.getArgument(0));

        var r = servicio.guardar(null, solicitud());

        assertEquals("Camisa manga corta hombre", r.nombre());
        assertEquals("hombre", r.genero());
        assertEquals(List.of("S", "M"), r.tallas());
        assertEquals(List.of("azul", "rojo"), r.colores());
        assertTrue(r.activo());
        assertEquals(1L, r.categoriaId());
    }

    @Test
    void rechazaGeneroYTipoRepetidos() {
        when(productos.existsByGeneroAndTipoAndIdNot("hombre", "cam_corta", -1L)).thenReturn(true);

        var e = assertThrows(ResponseStatusException.class, () -> servicio.guardar(null, solicitud()));

        assertEquals(409, estado(e));
        verify(productos, never()).saveAndFlush(any());
    }

    @Test
    void rechazaCategoriaInexistente() {
        when(categorias.findById(1L)).thenReturn(Optional.empty());

        var e = assertThrows(ResponseStatusException.class, () -> servicio.guardar(null, solicitud()));

        assertEquals(400, estado(e));
    }

    @Test
    void visitanteNoVeProductoInactivoPeroStaffSi() {
        when(productos.findById(5L)).thenReturn(Optional.of(producto(5L, false)));

        var e = assertThrows(ResponseStatusException.class, () -> servicio.obtener(5L, false));

        assertEquals(404, estado(e));
        assertEquals(5L, servicio.obtener(5L, true).id());
    }

    @Test
    void noEliminaProductoConVentas() {
        when(productos.findById(2L)).thenReturn(Optional.of(producto(2L, true)));
        when(detalles.existsByProductoId(2L)).thenReturn(true);

        var e = assertThrows(ResponseStatusException.class, () -> servicio.eliminar(2L));

        assertEquals(409, estado(e));
        verify(productos, never()).delete(any(Producto.class));
    }

    @Test
    void eliminaProductoSinVentas() {
        Producto p = producto(3L, true);
        when(productos.findById(3L)).thenReturn(Optional.of(p));
        when(detalles.existsByProductoId(3L)).thenReturn(false);

        servicio.eliminar(3L);

        verify(productos).delete(p);
    }
}
