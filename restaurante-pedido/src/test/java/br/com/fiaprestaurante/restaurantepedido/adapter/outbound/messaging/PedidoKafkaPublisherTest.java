package br.com.fiaprestaurante.restaurantepedido.adapter.outbound.messaging;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoCriadoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Testes unitarios do {@link PedidoKafkaPublisher} - usa mock do
 * {@link KafkaTemplate} para validar o topico e a chave da mensagem.
 *
 * @author Danilo Fernando
 */
class PedidoKafkaPublisherTest {

    private static final String TOPICO = "pedido.criado";
    private static final UUID PEDIDO_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID CLIENTE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");

    private KafkaTemplate<Object, Object> kafkaTemplate;
    private PedidoKafkaPublisher publisher;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        publisher = new PedidoKafkaPublisher(kafkaTemplate, TOPICO);
    }

    @Test
    void deveEnviarEventoComChavePedidoId() {
        PedidoCriadoEvent event = new PedidoCriadoEvent(
                PEDIDO_ID, CLIENTE_ID, new BigDecimal("59.30"), Instant.now());

        publisher.publicarPedidoCriado(event);

        verify(kafkaTemplate).send(TOPICO, PEDIDO_ID.toString(), event);
    }
}
