package br.com.fiaprestaurante.restauranteservice.adapter.outbound.messaging;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoEmPreparoEvent;
import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoProntoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Testes unitarios do {@link CozinhaKafkaPublisher}.
 *
 * @author Danilo Fernando
 */
class CozinhaKafkaPublisherTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private final KafkaTemplate<Object, Object> kafkaTemplate = (KafkaTemplate) mock(KafkaTemplate.class);

    private CozinhaKafkaPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new CozinhaKafkaPublisher(kafkaTemplate, "topic.em-preparo", "topic.pronto");
    }

    @Test
    void publicarEmPreparoDeveUsarPedidoIdComoChave() {
        UUID pedidoId = UUID.randomUUID();
        PedidoEmPreparoEvent event = new PedidoEmPreparoEvent(
                pedidoId, UUID.randomUUID(), UUID.randomUUID(), Instant.now());

        publisher.publicarEmPreparo(event);

        verify(kafkaTemplate).send(eq("topic.em-preparo"), eq(pedidoId.toString()), eq(event));
    }

    @Test
    void publicarProntoDeveUsarPedidoIdComoChave() {
        UUID pedidoId = UUID.randomUUID();
        PedidoProntoEvent event = new PedidoProntoEvent(
                pedidoId, UUID.randomUUID(), UUID.randomUUID(), Instant.now());

        publisher.publicarPronto(event);

        verify(kafkaTemplate).send(eq("topic.pronto"), eq(pedidoId.toString()), eq(event));
    }
}
