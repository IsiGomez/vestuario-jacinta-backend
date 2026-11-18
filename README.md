# Vestuario Jacinta · Spring Boot

Backend de la tienda **Vestuarios Jacinta**, desarrollado para la asignatura
**DSY1104 – Desarrollo Fullstack II** de DuocUC.

---

## Integrantes del Equipo

- **Isidora Gómez**
- **Lukas Calvo**

---

## Links

- [Repositorio del frontend](https://github.com/IsiGomez/vestuario-jacinta-react)
- [Frontend en GitHub Pages](https://isigomez.github.io/vestuario-jacinta-react/)
- [API desplegada (ejemplo: productos)](https://jacinta-backend-ceb8edg8efb5fcgg.westus-01.azurewebsites.net/api/productos)
- [Swagger desplegada](https://jacinta-backend-ceb8edg8efb5fcgg.westus-01.azurewebsites.net/swagger-ui/index.html)
- [Contrato de API de productos y órdenes](docs/contrato-api.md)

## Tecnologías utilizadas

- **Java 25** y **Spring Boot 4.1.2**.
- **Spring Web MVC** para la API REST.
- **Spring Data JPA / Hibernate** y **MySQL** para persistencia.
- **Spring Security**, **JJWT** y **BCrypt** para autenticación y permisos.
- **Validation**, **Lombok** y **springdoc-openapi**.
- **JUnit**, **Mockito** y **MockMvc** para pruebas.

## Cómo ejecutar el proyecto

Requisitos: **JDK 25**, **MySQL iniciado**, cliente `mysql` y Git.
Los comandos para generar la clave JWT usan OpenSSL. Maven se ejecuta con el wrapper incluido.

### 1. Descargar el proyecto

```bash
git clone https://github.com/IsiGomez/vestuario-jacinta-backend.git
cd vestuario-jacinta-backend
```

Si ya tienes la carpeta, abre una terminal dentro de ella.

### 2. Preparar MySQL

**Solo para una base nueva:** el siguiente script crea `jacinta_db`, sus cinco tablas
y los datos de prueba. **Borra las tablas existentes**, así que no lo repitas sobre
una base que quieras conservar.

```bash
mysql -u root -p < src/main/resources/db/db_jacinta.sql
```

Usa la contraseña de tu usuario MySQL. Si no tiene contraseña, presiona Enter.

La conexión se configura en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/jacinta_db?serverTimezone=America/Santiago&allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=
```

Ajusta usuario, contraseña o puerto si tu instalación usa otros valores.
Si `jacinta_db` ya existe, omite la importación y conserva sus datos.

Al iniciar el backend, `db/actualizacion_usuarios.sql` agrega `telefono` y `activo`
si faltan. No borra registros ni cambia las contraseñas. El usuario MySQL debe tener
permiso para agregar esas columnas.

### 3. Configurar JWT e iniciar

En **fish**:

```fish
set -gx JWT_SECRET (openssl rand -base64 32)
bash mvnw spring-boot:run
```

En **Bash**:

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
bash mvnw spring-boot:run
```

Conserva la misma clave entre reinicios para mantener válidos los tokens.
No guardes la clave en Git.

Si tu equipo usa **MariaDB**, inicia con el perfil correspondiente:

```bash
bash mvnw spring-boot:run -Dspring-boot.run.profiles=mariadb
```

Usa las mismas credenciales y la base `jacinta_db`. Sin este perfil se mantiene MySQL.

En **IntelliJ**, selecciona JDK 25 y agrega `JWT_SECRET` a las variables de entorno
de la configuración de ejecución. Después ejecuta `JacintaApplication`.

La API queda disponible en **http://localhost:8080/api**.

### 4. Conectar el frontend

En el `.env` del frontend:

```env
VITE_API_URL=http://localhost:8080/api
```

Ejecuta el frontend en `http://localhost:5173`. El origen permitido se configura en
`app.cors.allowed-origins`. Si cambias `.env`, reinicia Vite.

## Despliegue

El proyecto incluye un `Dockerfile` de dos etapas (compila con JDK 25 y ejecuta el JAR con
JRE 25, puerto `8080`). En el servidor, la configuración se entrega con variables de entorno,
que Spring Boot convierte en las propiedades de `application.properties`:

| Variable | Propiedad | Para qué sirve |
| :--- | :--- | :--- |
| `JWT_SECRET` | `jwt.secret` | Clave de firma de los tokens (obligatoria; no la guardes en Git) |
| `SPRING_DATASOURCE_URL` | `spring.datasource.url` | URL JDBC de la base de datos |
| `SPRING_DATASOURCE_USERNAME` | `spring.datasource.username` | Usuario de la base |
| `SPRING_DATASOURCE_PASSWORD` | `spring.datasource.password` | Contraseña de la base |
| `APP_CORS_ALLOWED_ORIGINS` | `app.cors.allowed-origins` | Orígenes del frontend permitidos, separados por coma |
| `PORT` | `server.port` | Puerto de la aplicación (por defecto `8080`) |

Para el frontend publicado en GitHub Pages:

```text
APP_CORS_ALLOWED_ORIGINS=https://isigomez.github.io,http://localhost:5173
```

Usa solo el origen (sin `/vestuario-jacinta-react/` ni barra final). Si no se define,
solo se acepta `http://localhost:5173` y el navegador bloqueará las peticiones del sitio publicado.
Tras cambiar variables, reinicia la aplicación.

### Comandos disponibles

Ejecutar desde la carpeta del backend:

| Comando | Descripción |
| :--- | :--- |
| `bash mvnw spring-boot:run` | Iniciar el backend con MySQL y JWT configurados |
| `bash mvnw test` | Ejecutar las pruebas |
| `bash mvnw clean package` | Compilar, probar y generar el JAR en `target/` |
| `java -jar target/jacinta-0.0.1-SNAPSHOT.jar` | Ejecutar el JAR con la misma configuración de MySQL y JWT |

En Windows puedes usar `mvnw.cmd` en lugar de `bash mvnw`.

## Credenciales de prueba

El script original de MySQL incluye estas cuentas:

| Correo | Contraseña | Rol |
| :--- | :--- | :--- |
| `admin@duoc.cl` | `1234` | `ADMINISTRADOR` |
| `vendedor@duoc.cl` | `1234` | `VENDEDOR` |
| `cliente@duoc.cl` | `1234` | `CLIENTE` |

Se usan las cuentas existentes: el backend no crea ni restablece usuarios al arrancar.
Los nuevos registros públicos siempre son `CLIENTE` y requieren una contraseña de
entre 8 y 24 caracteres.

## Estructura del proyecto

```text
vestuario-jacinta-backend/
├── pom.xml
├── mvnw / mvnw.cmd
├── Dockerfile
├── docs/
│   └── contrato-api.md     # Contrato de productos y órdenes
└── src/
    ├── main/
    │   ├── java/cl/duoc/jacinta/
    │   │   ├── JacintaApplication.java
    │   │   ├── config/         # Seguridad, CORS y Swagger
    │   │   ├── controller/     # Endpoints REST
    │   │   ├── service/        # Reglas de negocio
    │   │   ├── repository/     # Acceso a la base de datos
    │   │   ├── model/          # Entidades y enums
    │   │   ├── dto/            # Datos de entrada y salida
    │   │   ├── security/       # Filtro JWT y UserDetailsService
    │   │   └── exception/      # Respuestas de error
    │   └── resources/
    │       ├── application.properties
    │       └── db/
    │           ├── db_jacinta.sql
    │           └── actualizacion_usuarios.sql
    └── test/                  # Pruebas y configuración de tests
```

El flujo principal es **Controller → Service → Repository**. Los modelos representan
las tablas y los DTOs definen lo que recibe y devuelve la API.

## Funcionalidades implementadas

- Registro de clientes, login, renovación de tokens y consulta de la sesión actual.
- Roles `ADMINISTRADOR`, `VENDEDOR` y `CLIENTE`.
- CRUD de usuarios para el administrador y edición del perfil propio.
- Desactivación de usuarios para conservar el historial.
- Categorías públicas; solo el administrador puede crearlas, editarlas o eliminarlas.
- Rechazo de eliminación de categorías con productos asociados.
- Contraseñas almacenadas con BCrypt y omitidas en las respuestas.
- **Productos:** consulta pública de los productos activos; administrador y vendedor ven también
  los inactivos, crean y editan; solo el administrador elimina (409 si el producto ya tiene ventas).
  No se permiten dos productos con el mismo par `genero + tipo`.
- **Stock crítico:** `/api/productos/criticos` lista los productos con `stock <= stockCritico`.
- **Órdenes (boletas):** el cliente envía solo producto, cantidad, talla y color. El servidor toma
  el precio de la base de datos, calcula el total, valida talla, color y stock (409 si no alcanza)
  y reserva el stock. Se puede comprar como invitado o con sesión.
- **Estados de orden:** `PENDIENTE`, `PAGADA` y `PAGO_FALLIDO`. Pasar a `PAGO_FALLIDO` devuelve el
  stock; volver a `PENDIENTE` o `PAGADA` lo reserva de nuevo.
- Un cliente solo puede ver sus propias órdenes; administrador y vendedor ven todas.

El detalle de cuerpos, validaciones y errores está en [`docs/contrato-api.md`](docs/contrato-api.md).

## Endpoints principales

| Método | Ruta | Acceso |
| :--- | :--- | :--- |
| POST | `/api/auth/registro` | Público; crea clientes |
| POST | `/api/auth/login` | Público |
| POST | `/api/auth/refresh` | Refresh token válido |
| GET | `/api/auth/me` | Usuario autenticado |
| GET / POST | `/api/usuarios` | Administrador |
| GET / PUT / DELETE | `/api/usuarios/{id}` | Administrador |
| PUT | `/api/usuarios/perfil` | Usuario autenticado; solo su perfil |
| GET | `/api/categorias` y `/api/categorias/{id}` | Público |
| POST / PUT / DELETE | `/api/categorias` y `/api/categorias/{id}` | Administrador |
| GET | `/api/productos` y `/api/productos/{id}` | Público (solo activos; staff ve también inactivos) |
| GET | `/api/productos/criticos` | Administrador, vendedor |
| POST / PUT | `/api/productos` y `/api/productos/{id}` | Administrador, vendedor |
| DELETE | `/api/productos/{id}` | Administrador |
| POST | `/api/ordenes` | Público (el token es opcional) |
| GET | `/api/ordenes` | Administrador, vendedor |
| GET | `/api/ordenes/{id}` | Staff; el cliente solo si la orden es suya |
| GET | `/api/ordenes/usuario/{usuarioId}` | Staff o el propio usuario |
| PATCH | `/api/ordenes/{id}/estado` | Administrador, vendedor |

Swagger UI está en `/swagger-ui.html` y la especificación OpenAPI en `/v3/api-docs`.
En Swagger, realiza el login y copia `accessToken` en **Authorize**. Acceso JWT de
dos horas y refresh de siete días, según `application.properties`.

## Pruebas

```bash
bash mvnw test
```

Las pruebas verifican autenticación, permisos por rol, validaciones, usuarios, categorías,
productos, órdenes (cálculo de totales, reserva y devolución de stock), CORS y Swagger con
repositorios simulados, sin levantar MySQL. Las clases principales son `SeguridadIntegradaTest`,
`OrdenServiceTest` y `ProductoServiceTest`.
El arranque y la actualización real del esquema deben comprobarse también con MySQL.
