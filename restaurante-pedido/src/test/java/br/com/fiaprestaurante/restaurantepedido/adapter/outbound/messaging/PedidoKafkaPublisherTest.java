package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.messaging;

import br.com.fiaprestaurante.restaurantepedido.TestFixtures;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoCriadoEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PedidoKafkaPublisherTest {

    @Mock
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Test
    void devePublicarPedidoCriadoComChaveDoPedido() {
        PedidoKafkaPublisher publisher = new PedidoKafkaPublisher(kafkaTemplate, "pedido.criado");
        PedidoCriadoEvent event = new PedidoCriadoEvent(TestFixtures.PEDIDO_ID, TestFixtures.CLIENTE_ID,
                new BigDecimal("51.00"), Instant.parse("2026-05-21T10:10:00Z"));

        publisher.publicarPedidoCriado(event);

        verify(kafkaTemplate).send("pedido.criado", TestFixtures.PEDIDO_ID.toString(), event);
    }
}
