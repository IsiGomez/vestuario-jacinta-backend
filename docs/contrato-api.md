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
| GET | `/ordenes/{id}` | ADMIN, VENDEDOR; CLIENTE solo si es suya |
| GET | `/ordenes/usuario/{usuarioId}` | ADMIN, VENDEDOR o el propio usuario |
| PATCH | `/ordenes/{id}/estado` | ADMIN, VENDEDOR |

POST request (sin precio, sin usuarioId, sin estado: los pone el servidor):

```json
{
  "cliente": { "nombre": "Camila", "apellidos": "Rojas Pérez", "correo": "camila@mail.com" },
  "entrega": { "calle": "Los Aromos 1234", "departamento": "", "region": "...", "comuna": "...", "indicaciones": "" },
  "items": [{ "productoId": 1, "cantidad": 2, "talla": "M", "color": "azul" }]
}
```

Respuesta (OrdenResponse):

```json
{
  "id": 1, "numero": "2026111501", "fecha": "2026-11-15T15:30:00",
  "usuarioId": null, "estado": "PENDIENTE",
  "cliente": { "...": "..." }, "entrega": { "...": "..." },
  "items": [{ "productoId": 1, "nombre": "...", "precio": 14990, "cantidad": 2, "talla": "M", "color": "azul" }],
  "total": 29980
}
```

- Estados: `PENDIENTE`, `PAGADA`, `PAGO_FALLIDO`.
- Al crear, la orden queda `PENDIENTE` con el stock reservado. El precio sale de la BD.
- `usuarioId` sale del token si viene; si no, queda `null` (compra como invitado).
- `PATCH` body `{ "estado": "PAGADA" }`. `PAGO_FALLIDO` repone el stock. Volver a `PENDIENTE` o `PAGADA` lo reserva de nuevo.
- Errores: 400 validación, 404 producto inexistente o inactivo, 409 stock insuficiente.