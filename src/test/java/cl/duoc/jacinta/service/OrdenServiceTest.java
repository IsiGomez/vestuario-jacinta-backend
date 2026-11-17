package cl.duoc.jacinta.service;

import cl.duoc.jacinta.dto.orden.OrdenRequest;
import cl.duoc.jacinta.exception.RecursoNoEncontradoException;
import cl.duoc.jacinta.exception.ReglaNegocioException;
import cl.duoc.jacinta.model.*;
import cl.duoc.jacinta.repository.BoletaRepository;
import cl.duoc.jacinta.repository.ProductoRepository;
import cl.duoc.jacinta.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrdenServiceTest {
    private final BoletaRepository boletas = mock(BoletaRepository.class);
    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final OrdenService servicio = new OrdenService(boletas, productos, usuarios);

    @BeforeEach
    void guardarAsignaId() {
        when(boletas.saveAndFlush(any(Boleta.class))).thenAnswer(a -> {
            Boleta b = a.getArgument(0);
            b.setId(7L);
            return b;
        });
    }

    private Producto producto(long id, int precio, int stock) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre("Camisa");
        p.setPrecio(precio);
        p.setStock(stock);
        p.setTallas(new ArrayList<>(List.of("S", "M")));
        p.setColores(new ArrayList<>(List.of("azul", "rojo")));
        p.setActivo(true);
        Categoria c = new Categoria();
        c.setId(1L);
        p.setCategoria(c);
        when(productos.findByIdForUpdate(id)).thenReturn(Optional.of(p));
        return p;
    }

    private OrdenRequest.Item item(long productoId, int cantidad) {
        return new OrdenRequest.Item(productoId, cantidad, "M", "azul");
    }

    private OrdenRequest orden(OrdenRequest.Item... items) {
        return new OrdenRequest(
                new OrdenRequest.Cliente("Camila", "Rojas Pérez", "camila@mail.com"),
                new OrdenRequest.Entrega("Los Aromos 1234", "", "Región Metropolitana", "Providencia", ""),
                List.of(items));
    }

    // Orden ya creada con 2 unidades reservadas del producto
    private Boleta boletaCon(Producto p, EstadoBoleta estado, boolean reservado, Usuario dueno) {
        Boleta b = new Boleta();
        b.setId(7L);
        b.setEstado(estado);
        b.setStockReservado(reservado);
        b.setUsuario(dueno);
        DetalleBoleta d = new DetalleBoleta();
        d.setProducto(p);
        d.setNombreProducto(p.getNombre());
        d.setPrecioUnitario(14990);
        d.setCantidad(2);
        b.agregarDetalle(d);
        when(boletas.findByIdForUpdate(7L)).thenReturn(Optional.of(b));
        when(boletas.findWithDetallesById(7L)).thenReturn(Optional.of(b));
        return b;
    }

    private Usuario usuario(long id) {
        Usuario u = new Usuario();
        u.setId(id);
        return u;
    }

    @Test
    void usaElPrecioDeLaBaseDeDatosYDescuentaElStock() {
        Producto p = producto(1, 14990, 25);

        var r = servicio.crear(orden(item(1, 2)), null);

        assertEquals(EstadoBoleta.PENDIENTE, r.estado());
        assertEquals(29980, r.total().intValue());
        assertEquals(14990, r.items().get(0).precio().intValue());
        assertEquals(23, p.getStock().intValue());
        assertNull(r.usuarioId());
        assertEquals("azul", r.items().get(0).color());
        assertEquals(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "07", r.numero());
    }

    @Test
    void asociaLaOrdenAlUsuarioDelToken() {
        producto(1, 14990, 25);
        when(usuarios.getReferenceById(5L)).thenReturn(usuario(5L));

        var r = servicio.crear(orden(item(1, 1)), 5L);

        assertEquals(5L, r.usuarioId().longValue());
    }

    @Test
    void rechazaStockInsuficienteSinTocarNada() {
        Producto p = producto(1, 14990, 3);

        var e = assertThrows(ReglaNegocioException.class, () -> servicio.crear(orden(item(1, 5)), null));

        assertEquals("Solo quedan 3 unidades de \"Camisa\"", e.getMessage());
        assertEquals(3, p.getStock().intValue());
        verify(boletas, never()).saveAndFlush(any());
    }

    @Test
    void avisaCuandoElProductoEstaAgotado() {
        producto(1, 14990, 0);

        var e = assertThrows(ReglaNegocioException.class, () -> servicio.crear(orden(item(1, 1)), null));

        assertEquals("\"Camisa\" está agotado", e.getMessage());
    }

    @Test
    void sumaLasLineasDelMismoProductoAlValidarElStock() {
        producto(1, 14990, 3);

        assertThrows(ReglaNegocioException.class, () -> servicio.crear(orden(item(1, 2), item(1, 2)), null));
    }

    @Test
    void rechazaProductosInactivosOInexistentes() {
        Producto inactivo = producto(1, 14990, 10);
        inactivo.setActivo(false);

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.crear(orden(item(1, 1)), null));
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.crear(orden(item(99, 1)), null));
    }

    @Test
    void rechazaTallaOColorQueElProductoNoTiene() {
        Producto p = producto(1, 14990, 25);

        var talla = assertThrows(ResponseStatusException.class,
                () -> servicio.crear(orden(new OrdenRequest.Item(1L, 1, "XXL", "azul")), null));
        var color = assertThrows(ResponseStatusException.class,
                () -> servicio.crear(orden(new OrdenRequest.Item(1L, 1, "M", "verde")), null));

        assertEquals(400, talla.getStatusCode().value());
        assertEquals(400, color.getStatusCode().value());
        assertEquals(25, p.getStock().intValue());
    }

    @Test
    void elPagoFallidoRepoeElStockYVolverAPendienteLoReserva() {
        Producto p = producto(1, 14990, 23);
        Boleta b = boletaCon(p, EstadoBoleta.PENDIENTE, true, null);

        servicio.cambiarEstado(7L, EstadoBoleta.PAGO_FALLIDO);
        assertEquals(25, p.getStock().intValue());
        assertFalse(b.getStockReservado());
        assertEquals(EstadoBoleta.PAGO_FALLIDO, b.getEstado());

        servicio.cambiarEstado(7L, EstadoBoleta.PENDIENTE);
        assertEquals(23, p.getStock().intValue());
        assertTrue(b.getStockReservado());
    }

    @Test
    void pasarDePendienteAPagadaNoMueveElStock() {
        Producto p = producto(1, 14990, 23);
        boletaCon(p, EstadoBoleta.PENDIENTE, true, null);

        var r = servicio.cambiarEstado(7L, EstadoBoleta.PAGADA);

        assertEquals(EstadoBoleta.PAGADA, r.estado());
        assertEquals(23, p.getStock().intValue());
    }

    @Test
    void noVuelveAReservarSiYaNoHayStock() {
        Producto p = producto(1, 14990, 1);
        Boleta b = boletaCon(p, EstadoBoleta.PAGO_FALLIDO, false, null);

        assertThrows(ReglaNegocioException.class, () -> servicio.cambiarEstado(7L, EstadoBoleta.PENDIENTE));

        assertEquals(EstadoBoleta.PAGO_FALLIDO, b.getEstado());
        assertFalse(b.getStockReservado());
        assertEquals(1, p.getStock().intValue());
    }

    @Test
    void cambiarElEstadoDeUnaOrdenInexistenteDaNotFound() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.cambiarEstado(99L, EstadoBoleta.PAGADA));
    }

    @Test
    void unClienteSoloVeSusPropiasOrdenes() {
        Producto p = producto(1, 14990, 23);
        boletaCon(p, EstadoBoleta.PAGADA, true, usuario(5L));

        assertEquals(7L, servicio.obtener(7L, 5L, false).id().longValue());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(7L, 6L, false));
        assertEquals(7L, servicio.obtener(7L, 1L, true).id().longValue());
    }

    @Test
    void lasOrdenesDeInvitadoSoloLasVeElStaff() {
        Producto p = producto(1, 14990, 23);
        boletaCon(p, EstadoBoleta.PAGADA, true, null);

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.obtener(7L, 5L, false));
        assertEquals(7L, servicio.obtener(7L, 1L, true).id().longValue());
    }

    @Test
    void nadieListaLasOrdenesDeOtroUsuarioSalvoElStaff() {
        Producto p = producto(1, 14990, 23);
        Boleta b = boletaCon(p, EstadoBoleta.PAGADA, true, usuario(5L));
        when(boletas.findByUsuarioIdOrderByFechaDesc(5L)).thenReturn(List.of(b));

        assertThrows(AccessDeniedException.class, () -> servicio.porUsuario(5L, 6L, false));
        assertEquals(1, servicio.porUsuario(5L, 5L, false).size());
        assertEquals(1, servicio.porUsuario(5L, 1L, true).size());
    }
}