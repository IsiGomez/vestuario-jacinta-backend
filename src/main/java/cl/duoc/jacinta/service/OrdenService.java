package cl.duoc.jacinta.service;

import cl.duoc.jacinta.dto.orden.OrdenRequest;
import cl.duoc.jacinta.dto.orden.OrdenResponse;
import cl.duoc.jacinta.exception.RecursoNoEncontradoException;
import cl.duoc.jacinta.exception.ReglaNegocioException;
import cl.duoc.jacinta.model.Boleta;
import cl.duoc.jacinta.model.DetalleBoleta;
import cl.duoc.jacinta.model.EstadoBoleta;
import cl.duoc.jacinta.model.Producto;
import cl.duoc.jacinta.repository.BoletaRepository;
import cl.duoc.jacinta.repository.ProductoRepository;
import cl.duoc.jacinta.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrdenService {
    private static final Set<EstadoBoleta> RESERVAN = EnumSet.of(EstadoBoleta.PENDIENTE, EstadoBoleta.PAGADA);

    private final BoletaRepository boletas;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;

    @Transactional
    public OrdenResponse crear(OrdenRequest d, Long usuarioId) {
        Map<Long, Integer> pedido = new TreeMap<>();
        for (var item : d.items())
            pedido.merge(item.productoId(), item.cantidad(), Integer::sum);

        Map<Long, Producto> catalogo = bloquear(pedido, true);

        Boleta b = new Boleta();
        b.setNumero(UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        b.setEstado(EstadoBoleta.PENDIENTE);
        b.setStockReservado(true);
        if (usuarioId != null)
            b.setUsuario(usuarios.getReferenceById(usuarioId));
        var c = d.cliente();
        b.setClienteNombre(c.nombre().strip());
        b.setClienteApellidos(c.apellidos().strip());
        b.setClienteCorreo(c.correo().strip());
        var e = d.entrega();
        b.setEntregaCalle(e.calle().strip());
        b.setEntregaDepartamento(limpio(e.departamento()));
        b.setEntregaRegion(limpio(e.region()));
        b.setEntregaComuna(limpio(e.comuna()));
        b.setEntregaIndicaciones(limpio(e.indicaciones()));

        for (var item : d.items()) {
            Producto p = catalogo.get(item.productoId());
            DetalleBoleta det = new DetalleBoleta();
            det.setProducto(p);
            det.setNombreProducto(p.getNombre());
            det.setPrecioUnitario(p.getPrecio());
            det.setCantidad(item.cantidad());
            det.setTalla(opcion(p.getTallas(), item.talla(), "talla", p));
            det.setColor(opcion(p.getColores(), item.color(), "color", p));
            b.agregarDetalle(det);
        }

        pedido.forEach((id, cantidad) -> {
            Producto p = catalogo.get(id);
            p.setStock(p.getStock() - cantidad);
        });

        Boleta guardada = boletas.saveAndFlush(b);
        guardada.setNumero(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%02d", guardada.getId()));
        return respuesta(guardada);
    }

    @Transactional
    public OrdenResponse cambiarEstado(Long id, EstadoBoleta nuevo) {
        Boleta b = boletas.findByIdForUpdate(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        boolean debeReservar = RESERVAN.contains(nuevo);
        Map<Long, Integer> pedido = agrupar(b.getDetalles());

        if (b.getStockReservado() && !debeReservar) {
            for (var entrada : pedido.entrySet()) {
                Producto p = productos.findByIdForUpdate(entrada.getKey())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
                p.setStock(p.getStock() + entrada.getValue());
            }
            b.setStockReservado(false);
        } else if (!b.getStockReservado() && debeReservar) {
            bloquear(pedido, false).forEach((pid, p) -> p.setStock(p.getStock() - pedido.get(pid)));
            b.setStockReservado(true);
        }
        b.setEstado(nuevo);
        return respuesta(b);
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listar() {
        return boletas.findAllByOrderByFechaDesc().stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public OrdenResponse obtener(Long id, Long solicitanteId, boolean staff) {
        Boleta b = boletas.findWithDetallesById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada"));
        if (!staff && (b.getUsuario() == null || !b.getUsuario().getId().equals(solicitanteId)))
            throw new RecursoNoEncontradoException("Orden no encontrada");
        return respuesta(b);
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> porUsuario(Long usuarioId, Long solicitanteId, boolean staff) {
        if (!staff && !usuarioId.equals(solicitanteId))
            throw new AccessDeniedException("No puedes ver las órdenes de otro usuario");
        return boletas.findByUsuarioIdOrderByFechaDesc(usuarioId).stream().map(this::respuesta).toList();
    }

    private Map<Long, Producto> bloquear(Map<Long, Integer> pedido, boolean exigirActivo) {
        Map<Long, Producto> bloqueados = new LinkedHashMap<>();
        for (var entrada : pedido.entrySet()) {
            Producto p = productos.findByIdForUpdate(entrada.getKey())
                    .filter(x -> !exigirActivo || x.getActivo())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Un producto de tu carrito ya no está disponible"));
            if (p.getStock() < entrada.getValue())
                throw new ReglaNegocioException(p.getStock() == 0
                        ? "\"" + p.getNombre() + "\" está agotado"
                        : "Solo quedan " + p.getStock() + " unidades de \"" + p.getNombre() + "\"");
            bloqueados.put(entrada.getKey(), p);
        }
        return bloqueados;
    }

    private Map<Long, Integer> agrupar(List<DetalleBoleta> detalles) {
        Map<Long, Integer> pedido = new TreeMap<>();
        for (DetalleBoleta d : detalles)
            pedido.merge(d.getProducto().getId(), d.getCantidad(), Integer::sum);
        return pedido;
    }

    private String opcion(List<String> validas, String valor, String campo, Producto p) {
        return validas.stream().filter(v -> v.equalsIgnoreCase(valor.strip())).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El producto \"" + p.getNombre() + "\" no tiene " + campo + " \"" + valor + "\""));
    }

    private static String limpio(String v) {
        return v == null ? "" : v.strip();
    }

    public OrdenResponse respuesta(Boleta b) {
        var items = b.getDetalles().stream()
                .map(d -> new OrdenResponse.Item(d.getProducto().getId(), d.getNombreProducto(), d.getPrecioUnitario(),
                        d.getCantidad(), d.getTalla(), d.getColor()))
                .toList();
        return new OrdenResponse(b.getId(), b.getNumero(), b.getFecha(),
                b.getUsuario() == null ? null : b.getUsuario().getId(), b.getEstado(),
                new OrdenResponse.Cliente(b.getClienteNombre(), b.getClienteApellidos(), b.getClienteCorreo()),
                new OrdenResponse.Entrega(b.getEntregaCalle(), limpio(b.getEntregaDepartamento()), b.getEntregaRegion(),
                        b.getEntregaComuna(), limpio(b.getEntregaIndicaciones())),
                items, b.calcularTotal());
    }
}