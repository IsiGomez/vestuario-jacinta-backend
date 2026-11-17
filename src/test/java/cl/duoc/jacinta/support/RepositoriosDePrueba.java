package cl.duoc.jacinta.support;

import cl.duoc.jacinta.repository.BoletaRepository;
import cl.duoc.jacinta.repository.CategoriaRepository;
import cl.duoc.jacinta.repository.DetalleBoletaRepository;
import cl.duoc.jacinta.repository.ProductoRepository;
import cl.duoc.jacinta.repository.UsuarioRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class RepositoriosDePrueba {
    @Bean
    UsuarioRepository usuarios() {
        return mock(UsuarioRepository.class);
    }

    @Bean
    CategoriaRepository categorias() {
        return mock(CategoriaRepository.class);
    }

    @Bean
    ProductoRepository productos() {
        return mock(ProductoRepository.class);
    }

    @Bean
    DetalleBoletaRepository detallesBoleta() {
        return mock(DetalleBoletaRepository.class);
    }

    @Bean
    BoletaRepository boletas() {
        return mock(BoletaRepository.class);
    }
}
