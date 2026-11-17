# Contrato de API · Productos y órdenes

Base: `http://localhost:8080/api`. Errores: `{ status, error, mensaje, detalles? }`.
Roles en mayúscula: `ADMINISTRADOR`, `VENDEDOR`, `CLIENTE`.

## Productos

| Método | Ruta | Acceso |
| :--- | :--- | :--- |
| GET | `/productos` | Público (solo activos). Con token ADMIN/VENDEDOR devuelve también los inactivos |
| GET | `/productos/{id}` | Público (solo activos). Staff también ve inactivos |
| GET | `/productos/criticos` | ADMIN, VENDEDOR (stock <= stockCritico) |
| POST | `/productos` | ADMIN, VENDEDOR → 201 |
| PUT | `/productos/{id}` | ADMIN, VENDEDOR |
| DELETE | `/productos/{id}` | ADMIN → 204. 409 si tiene ventas |

ProductoRequest / ProductoResponse (la respuesta suma `id`):

```json
{
  "id": 1,
  "nombre": "Camisa manga corta hombre",
  "genero": "hombre",
  "tipo": "cam_corta",
  "categoriaId": 1,
  "precio": 14990,
  "stock": 25,
  "stockCritico": 5,
  "descripcion": "Camisa de manga corta...",
  "tallas": ["S", "M", "L", "XL"],
  "colores": ["azul", "rojo"],
  "activo": true
}
```

- `colores` son solo claves. El front arma `{nombre, clave, imagenes}` a partir de ellas.
- Validaciones: nombre 1-150, precio >= 0, stock >= 0, tallas y colores no vacíos, categoría existente.
- 409 si ya existe el par `genero + tipo`.

## Órdenes

| Método | Ruta | Acceso |
| :--- | :--- | :--- |
| POST | `/ordenes` | Público (token opcional) → 201 |
| GET | `/ordenes` | ADMIN, VENDEDOR |
| GET | `/ordenes/{id}` | ADMIN, VENDEDOR; CLIENTE solo si es suya (si no, 404) |
| GET | `/ordenes/usuario/{usuarioId}` | ADMIN, VENDEDOR o el propio usuario (si no, 403) |
| PATCH | `/ordenes/{id}/estado` | ADMIN, VENDEDOR |

### POST /ordenes

El cliente NO envía precio, usuario ni estado. Los decide el servidor.

```json
{
  "cliente": { "nombre": "Camila", "apellidos": "Rojas Pérez", "correo": "camila@mail.com" },
  "entrega": { "calle": "Los Aromos 1234", "departamento": "", "region": "Región Metropolitana", "comuna": "Providencia", "indicaciones": "" },
  "items": [{ "productoId": 1, "cantidad": 2, "talla": "M", "color": "azul" }]
}
```

- `color` es la clave del color (`azul`), la misma que devuelve `/productos`. `talla` y `color` son obligatorios y deben existir en el producto (si no, 400).
- `region` y `comuna` pueden ir vacías (retiro en tienda); `calle` es obligatoria.
- `usuarioId` sale del token si viene; si no, la orden queda sin usuario (compra como invitado). Un `usuarioId` en el cuerpo se ignora.
- Respuesta (OrdenResponse): `id, numero, fecha, usuarioId, estado, cliente, entrega, items[{productoId, nombre, precio, cantidad, talla, color}], total`.
- `numero` = fecha (aaaammdd) + id con 2 dígitos mínimo. `total` lo calcula el servidor.
- Queda `PENDIENTE` con el stock reservado. El precio sale de la BD.
- Errores: 400 validación o talla/color inexistente, 404 producto inexistente o inactivo, 409 stock insuficiente (mensaje listo para mostrar al cliente).

### PATCH /ordenes/{id}/estado

Body `{ "estado": "PAGADA" }`. 

Estados: `PENDIENTE`, `PAGADA`, `PAGO_FALLIDO`.

- `PENDIENTE` y `PAGADA` tienen el stock reservado; `PAGO_FALLIDO` lo devuelve.
- Pasar a `PAGO_FALLIDO` repone el stock. Volver a `PENDIENTE` o `PAGADA` lo reserva de nuevo (409 si ya no alcanza).
- Entre `PENDIENTE` y `PAGADA` el stock no cambia.
- 404 si la orden no existe.