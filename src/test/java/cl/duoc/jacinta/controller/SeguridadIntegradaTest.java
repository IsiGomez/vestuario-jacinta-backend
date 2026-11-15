package cl.duoc.jacinta.controller;

import cl.duoc.jacinta.model.*;
import cl.duoc.jacinta.repository.*;
import cl.duoc.jacinta.service.JwtService;
import cl.duoc.jacinta.support.RepositoriosDePrueba;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(RepositoriosDePrueba.class)
class SeguridadIntegradaTest {
    @Autowired
    MockMvc mvc;
    @Autowired
    UsuarioRepository usuarios;
    @Autowired
    CategoriaRepository categorias;
    @Autowired
    JwtService jwt;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    ObjectMapper json;

    private final Map<Long, Usuario> datosUsuarios = new HashMap<>();
    private final Map<Long, Categoria> datosCategorias = new HashMap<>();
    private final Map<Long, Long> productosPorCategoria = new HashMap<>();
    private Usuario admin, vendedor, cliente;

    @BeforeEach
    void prepararRepositoriosSimulados() {
        reset(usuarios, categorias);
        datosUsuarios.clear();
        datosCategorias.clear();
        productosPorCategoria.clear();
        AtomicLong idUsuario = new AtomicLong();
        AtomicLong idCategoria = new AtomicLong();
        when(usuarios.findById(anyLong())).thenAnswer(a -> Optional.ofNullable(datosUsuarios.get(a.getArgument(0))));
        when(usuarios.findByCorreo(anyString()))
            .thenAnswer(a
                -> datosUsuarios.values()
                    .stream()
                    .filter(u -> u.getCorreo().equalsIgnoreCase(a.getArgument(0)))
                    .findFirst());
        when(usuarios.findAll()).thenAnswer(a -> new ArrayList<>(datosUsuarios.values()));
        when(usuarios.existsByCorreoAndIdNot(anyString(), anyLong()))
            .thenAnswer(a
                -> datosUsuarios.values().stream().anyMatch(
                    u -> u.getCorreo().equalsIgnoreCase(a.getArgument(0)) && !u.getId().equals(a.getArgument(1))));
        when(usuarios.existsByRunAndIdNot(anyString(), anyLong()))
            .thenAnswer(a
                -> datosUsuarios.values().stream().anyMatch(
                    u -> u.getRun().equals(a.getArgument(0)) && !u.getId().equals(a.getArgument(1))));
        when(usuarios.saveAndFlush(any(Usuario.class))).thenAnswer(a -> {
            Usuario u = a.getArgument(0);
            if (u.getId() == null)
                u.setId(idUsuario.incrementAndGet());
            datosUsuarios.put(u.getId(), u);
            return u;
        });
        when(categorias.findAll()).thenAnswer(a -> new ArrayList<>(datosCategorias.values()));
        when(categorias.findById(anyLong()))
            .thenAnswer(a -> Optional.ofNullable(datosCategorias.get(a.getArgument(0))));
        when(categorias.contarProductos(anyLong()))
            .thenAnswer(a -> productosPorCategoria.getOrDefault(a.getArgument(0), 0L));
        when(categorias.existsByNombreIgnoreCaseAndIdNot(anyString(), anyLong()))
            .thenAnswer(a
                -> datosCategorias.values().stream().anyMatch(
                    c -> c.getNombre().equalsIgnoreCase(a.getArgument(0)) && !c.getId().equals(a.getArgument(1))));
        when(categorias.saveAndFlush(any(Categoria.class))).thenAnswer(a -> {
            Categoria c = a.getArgument(0);
            if (c.getId() == null)
                c.setId(idCategoria.incrementAndGet());
            datosCategorias.put(c.getId(), c);
            return c;
        });
        doAnswer(a -> {
            datosCategorias.remove(((Categoria) a.getArgument(0)).getId());
            return null;
        })
            .when(categorias)
            .delete(any(Categoria.class));
        admin = crear("admin@duoc.cl", "190110222", TipoUsuario.ADMINISTRADOR);
        admin.setContrasena("$2b$10$l0Lx.fhOARhCcccwBhEMKODba2RAm7pqKxTNthCGz0rRU.510BRO2");
        vendedor = crear("vendedor@duoc.cl", "180220333", TipoUsuario.VENDEDOR);
        cliente = crear("cliente@duoc.cl", "170330444", TipoUsuario.CLIENTE);
    }

    Usuario crear(String correo, String run, TipoUsuario tipo) {
        Usuario u = new Usuario();
        u.setCorreo(correo);
        u.setRun(run);
        u.setNombre("Ana");
        u.setApellidos("Perez");
        u.setTelefono("+56912345678");
        u.setRegion("Valparaíso");
        u.setComuna("Viña del Mar");
        u.setDireccion("Calle de prueba 123");
        u.setTipoUsuario(tipo);
        u.setContrasena(encoder.encode("ClavePrueba123"));
        return usuarios.saveAndFlush(u);
    }

    record Res(int status, Map<?, ?> body) {}

    Res req(String method, String path, Object datos, String token) throws Exception {
        var request = org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                          .request(org.springframework.http.HttpMethod.valueOf(method), path)
                          .contentType("application/json");
        if (datos != null)
            request.content(json.writeValueAsString(datos));
        if (token != null)
            request.header("Authorization", "Bearer " + token);
        var res = mvc.perform(request).andReturn().getResponse();
        String body = res.getContentAsString();
        return new Res(res.getStatus(), body.startsWith("{") ? json.readValue(body, Map.class) : Map.of());
    }

    Map<String, Object> datos() {
        return new HashMap<>(Map.of("correo", "nuevo@duoc.cl", "run", "12345678-5", "nombre", "Nuevo", "apellidos",
            "Perez", "telefono", "+56912345678", "region", "Valparaíso", "comuna", "Viña del Mar", "direccion",
            "Calle de prueba 123", "contrasena", "OtraClave123", "tipoUsuario", "VENDEDOR"));
    }

    @Test
    void loginConCuentaOriginalRefreshYMe() throws Exception {
        assertTrue(encoder.matches("1234", admin.getContrasena()));
        var login = req("POST", "/api/auth/login", Map.of("correo", "ADMIN@duoc.cl", "contrasena", "1234"), null);
        assertEquals(200, login.status());
        assertEquals(7200, ((Number) login.body().get("expiresIn")).intValue());
        String access = (String) login.body().get("accessToken"), refresh = (String) login.body().get("refreshToken");
        var me = req("GET", "/api/auth/me", null, access);
        assertEquals(200, me.status());
        assertEquals("ADMINISTRADOR", me.body().get("tipoUsuario"));
        assertFalse(me.body().containsKey("contrasena"));
        assertEquals(200, req("POST", "/api/auth/refresh", Map.of("refreshToken", refresh), null).status());
        assertEquals(401, req("POST", "/api/auth/refresh", Map.of("refreshToken", access), null).status());
        assertEquals(401, req("GET", "/api/auth/me", null, refresh).status());
        assertEquals(401,
            req("POST", "/api/auth/login", Map.of("correo", admin.getCorreo(), "contrasena", "mal"), null).status());
    }

    @Test
    void registroClienteValidaYHasheaSinModificarCuentasOriginales() throws Exception {
        var registro = req("POST", "/api/auth/registro", datos(), null);
        assertEquals(201, registro.status());
        assertEquals("CLIENTE", registro.body().get("tipoUsuario"));
        assertFalse(registro.body().containsKey("contrasena"));
        var nuevo = usuarios.findByCorreo("nuevo@duoc.cl").orElseThrow();
        assertTrue(encoder.matches("OtraClave123", nuevo.getContrasena()));
        assertEquals("123456785", nuevo.getRun());
        assertEquals(409, req("POST", "/api/auth/registro", datos(), null).status());
        assertTrue(encoder.matches("1234", admin.getContrasena()));
        assertEquals(4, datosUsuarios.size());
    }

    @Test
    void validacionesCoincidenConColumnasMysql() throws Exception {
        for (var cambio :
            Map.of("nombre", "x".repeat(51), "correo", "x".repeat(95) + "@duoc.cl", "region", "x".repeat(101), "run",
                   "123456789", "contrasena", "á".repeat(40))
                .entrySet()) {
            var d = datos();
            d.put(cambio.getKey(), cambio.getValue());
            assertEquals(400, req("POST", "/api/auth/registro", d, null).status());
        }
        assertEquals(400, req("POST", "/api/auth/registro", Map.of(), null).status());
        assertEquals(3, datosUsuarios.size());
    }

    @Test
    void usuariosSoloAdminYPerfilSinEscalada() throws Exception {
        String ta = jwt.acceso(admin), tv = jwt.acceso(vendedor), tc = jwt.acceso(cliente);
        assertEquals(401, req("GET", "/api/usuarios", null, null).status());
        assertEquals(403, req("GET", "/api/usuarios", null, tv).status());
        assertEquals(403, req("POST", "/api/usuarios", datos(), tc).status());
        var creado = req("POST", "/api/usuarios", datos(), ta);
        assertEquals(201, creado.status());
        Long id = ((Number) creado.body().get("id")).longValue();
        String hash = datosUsuarios.get(id).getContrasena();
        var d = datos();
        d.put("nombre", "Editado");
        d.put("contrasena", "");
        assertEquals(200, req("PUT", "/api/usuarios/" + id, d, ta).status());
        assertEquals(hash, datosUsuarios.get(id).getContrasena());
        d.put("contrasena", "CambioClave123");
        assertEquals(200, req("PUT", "/api/usuarios/" + id, d, ta).status());
        assertTrue(encoder.matches("CambioClave123", datosUsuarios.get(id).getContrasena()));
        d.put("correo", cliente.getCorreo());
        d.put("run", "17033044-7");
        d.put("tipoUsuario", "ADMINISTRADOR");
        assertEquals(200, req("PUT", "/api/usuarios/perfil", d, tc).status());
        assertEquals(TipoUsuario.CLIENTE, cliente.getTipoUsuario());
        assertEquals(409, req("DELETE", "/api/usuarios/" + admin.getId(), null, ta).status());
        assertEquals(204, req("DELETE", "/api/usuarios/" + id, null, ta).status());
        assertFalse(datosUsuarios.get(id).isActivo());
        assertEquals(404, req("GET", "/api/usuarios/" + id, null, ta).status());
    }

    @Test
    void usuariosInactivosYRolActualSeCompruebanConCadaToken() throws Exception {
        String access = jwt.acceso(vendedor), refresh = jwt.refresh(vendedor);
        assertEquals(403, req("GET", "/api/usuarios", null, access).status());
        vendedor.setTipoUsuario(TipoUsuario.ADMINISTRADOR);
        assertEquals(200, req("GET", "/api/usuarios", null, access).status());
        vendedor.setActivo(false);
        assertEquals(401, req("GET", "/api/auth/me", null, access).status());
        assertEquals(401, req("POST", "/api/auth/refresh", Map.of("refreshToken", refresh), null).status());
    }

    @Test
    void categoriasPublicasVendedorSoloLeeYProductosImpidenEliminar() throws Exception {
        String ta = jwt.acceso(admin), tv = jwt.acceso(vendedor);
        assertEquals(200, req("GET", "/api/categorias", null, null).status());
        assertEquals(403, req("POST", "/api/categorias", Map.of("nombre", "Camisas"), tv).status());
        var creado = req("POST", "/api/categorias", Map.of("nombre", "Camisas"), ta);
        assertEquals(201, creado.status());
        Long id = ((Number) creado.body().get("id")).longValue();
        assertEquals(200, req("GET", "/api/categorias/" + id, null, tv).status());
        assertEquals(409, req("POST", "/api/categorias", Map.of("nombre", "CAMISAS"), ta).status());
        productosPorCategoria.put(id, 1L);
        assertEquals(409, req("DELETE", "/api/categorias/" + id, null, ta).status());
        assertEquals(403, req("DELETE", "/api/categorias/" + id, null, tv).status());
        productosPorCategoria.clear();
        assertEquals(200, req("PUT", "/api/categorias/" + id, Map.of("nombre", "Poleras"), ta).status());
        assertEquals(204, req("DELETE", "/api/categorias/" + id, null, ta).status());
    }

    @Test
    void rechazaTokensVencidosAlteradosYFirmasAjenas() throws Exception {
        var key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
            io.jsonwebtoken.io.Decoders.BASE64.decode("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="));
        String vencido = Jwts.builder()
                             .issuer("jacinta")
                             .subject(cliente.getId().toString())
                             .claim("tipo", "access")
                             .issuedAt(new Date(0))
                             .expiration(new Date(1))
                             .signWith(key)
                             .compact();
        assertEquals(401, req("GET", "/api/auth/me", null, vencido).status());
        String falso = Jwts.builder()
                           .issuer("jacinta")
                           .subject(cliente.getId().toString())
                           .claim("tipo", "access")
                           .issuedAt(new Date())
                           .expiration(new Date(System.currentTimeMillis() + 60000))
                           .signWith(Jwts.SIG.HS256.key().build())
                           .compact();
        assertEquals(401, req("GET", "/api/auth/me", null, falso).status());
        assertEquals(401, req("GET", "/api/auth/me", null, "falso").status());
    }

    @Test
    void corsYSwaggerConservanConfiguracionOriginal() throws Exception {
        var preflight = mvc.perform(options("/api/auth/registro")
                                        .header("Origin", "http://localhost:5173")
                                        .header("Access-Control-Request-Method", "POST")
                                        .header("Access-Control-Request-Headers", "content-type"))
                            .andReturn()
                            .getResponse();
        assertEquals(200, preflight.getStatus());
        assertEquals("http://localhost:5173", preflight.getHeader("Access-Control-Allow-Origin"));
        var noPermitido = mvc.perform(options("/api/auth/registro")
                                          .header("Origin", "https://otro.example")
                                          .header("Access-Control-Request-Method", "POST"))
                              .andReturn()
                              .getResponse();
        assertEquals(403, noPermitido.getStatus());
        var swagger = req("GET", "/v3/api-docs", null, null);
        assertEquals(200, swagger.status());
        assertTrue(swagger.body().toString().contains("bearerAuth"));
    }
    @Test
    void longitudesSeValidanEnCaracteresYAdmitenTextoNoAscii() throws Exception {
        var d = datos();
        d.put("nombre", "a".repeat(50));
        d.put("contrasena", "界".repeat(24));
        var creado = req("POST", "/api/auth/registro", d, null);
        assertEquals(201, creado.status());
        var nuevo = usuarios.findByCorreo("nuevo@duoc.cl").orElseThrow();
        assertTrue(encoder.matches("界".repeat(24), nuevo.getContrasena()));
        assertEquals(200, req("POST", "/api/auth/login",
            Map.of("correo", "nuevo@duoc.cl", "contrasena", "界".repeat(24)), null).status());

        d.put("contrasena", "界".repeat(25));
        var claveLarga = req("POST", "/api/auth/registro", d, null);
        assertEquals(400, claveLarga.status());
        assertTrue(claveLarga.body().get("detalles").toString().contains("24 caracteres"));
        assertFalse(claveLarga.body().toString().contains("bytes"));

        d.put("contrasena", "ClaveValida123");
        d.put("nombre", "a".repeat(51));
        var nombreLargo = req("POST", "/api/auth/registro", d, null);
        assertEquals(400, nombreLargo.status());
        assertTrue(nombreLargo.body().get("detalles").toString().contains("50 caracteres"));
    }
}
