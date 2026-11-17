package cl.duoc.jacinta.service;

import cl.duoc.jacinta.dto.producto.*;
import cl.duoc.jacinta.model.Producto;
import cl.duoc.jacinta.repository.CategoriaRepository;
import cl.duoc.jacinta.repository.DetalleBoletaRepository;
import cl.duoc.jacinta.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {
    private final ProductoRepository productos;
    private final CategoriaRepository categorias;
    private final DetalleBoletaRepository detalles;

    private Producto buscar(Long id) {
        return productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Producto no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(boolean staff) {
        List<Producto> lista = staff ? productos.findAll() : productos.findByActivoTrue();
        return lista.stream().sorted(Comparator.comparing(Producto::getId)).map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id, boolean staff) {
        Producto p = buscar(id);
        if (!staff && !p.getActivo())
            throw new ResponseStatusException(NOT_FOUND, "Producto no encontrado");
        return respuesta(p);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> criticos() {
        return productos.findCriticos().stream().map(this::respuesta).toList();
    }

    @Transactional
    public ProductoResponse guardar(Long id, ProductoRequest d) {
        String genero = d.genero().strip().toLowerCase(Locale.ROOT);
        String tipo = d.tipo().strip();
        if (productos.existsByGeneroAndTipoAndIdNot(genero, tipo, id == null ? -1L : id))
            throw new ResponseStatusException(CONFLICT, "Ya existe un producto de este género y tipo");
        var categoria = categorias.findById(d.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "La categoría no existe"));

        Producto p = id == null ? new Producto() : buscar(id);
        p.setNombre(d.nombre().strip());
        p.setGenero(genero);
        p.setTipo(tipo);
        p.setCategoria(categoria);
        p.setPrecio(d.precio());
        p.setStock(d.stock());
        p.setStockCritico(d.stockCritico());
        p.setDescripcion(d.descripcion() == null ? "" : d.descripcion().strip());
        p.setTallas(limpiar(d.tallas(), false));
        p.setColores(limpiar(d.colores(), true));
        if (d.activo() != null)
            p.setActivo(d.activo());
        return respuesta(productos.saveAndFlush(p));
    }

    @Transactional
    public void eliminar(Long id) {
        Producto p = buscar(id);
        if (detalles.existsByProductoId(id))
            throw new ResponseStatusException(CONFLICT,
                    "El producto tiene ventas registradas: desactívalo en lugar de eliminarlo");
        productos.delete(p);
        productos.flush();
    }

    public ProductoResponse respuesta(Producto p) {
        return new ProductoResponse(p.getId(), p.getNombre(), p.getGenero(), p.getTipo(),
                p.getCategoria().getId(), p.getPrecio(), p.getStock(), p.getStockCritico(), p.getDescripcion(),
                p.getTallas(), p.getColores(), p.getActivo());
    }

    private List<String> limpiar(List<String> valores, boolean minusculas) {
        return valores.stream()
                .map(v -> minusculas ? v.strip().toLowerCase(Locale.ROOT) : v.strip())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }
}