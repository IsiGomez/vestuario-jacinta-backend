package cl.duoc.jacinta;

import org.junit.jupiter.api.Test;
import cl.duoc.jacinta.support.RepositoriosDePrueba;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Import(RepositoriosDePrueba.class)
class JacintaApplicationTests {

	@Test
	void contextLoads() {
	}

}
