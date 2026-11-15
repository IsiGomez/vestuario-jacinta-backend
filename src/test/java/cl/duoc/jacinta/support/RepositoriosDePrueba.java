package cl.duoc.jacinta.support;

import cl.duoc.jacinta.repository.CategoriaRepository;
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
}
