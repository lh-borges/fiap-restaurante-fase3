package br.com.fiaprestaurante.restauranteservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test do bootstrap — garante que o ApplicationContext sobe
 * com H2 + EmbeddedKafka, sem exigir MySQL/Kafka externos.
 *
 * @author Danilo Fernando
 */
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {
        "pedido.pronto-para-cozinha",
        "pedido.em-preparo",
        "pedido.pronto"
})
@ActiveProfiles("test")
class RestauranteServiceApplicationTests {

    @Test
    void contextLoads() {
        // pass — se chegou aqui, todos os beans foram criados com sucesso
    }
}
