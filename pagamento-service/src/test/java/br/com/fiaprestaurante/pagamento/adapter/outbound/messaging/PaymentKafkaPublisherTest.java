package br.com.fiaprestaurante.pagamento.adapter.outbound.messaging;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoAprovadoEvent;
import br.com.fiaprestaurante.pagamento.application.dto.PagamentoPendenteEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentKafkaPublisherTest {

    @Mock
    private KafkaTemplate<Object, Object> kafkaTemplate;

    private PaymentKafkaPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new PaymentKafkaPublisher(kafkaTemplate, "pagamento.aprovado", "pagamento.pendente");
    }

    @Test
    void devePublicarPagamentoAprovadoComPedidoIdComoChave() {
        UUID pedidoId = UUID.randomUUID();
        PagamentoAprovadoEvent event = new PagamentoAprovadoEvent(pedidoId, UUID.randomUUID(), Instant.now());

        publisher.publicarPagamentoAprovado(event);

        verify(kafkaTemplate).send("pagamento.aprovado", pedidoId.toString(), event);
    }

    @Test
    void devePublicarPagamentoPendenteComPedidoIdComoChave() {
        UUID pedidoId = UUID.randomUUID();
        PagamentoPendenteEvent event = new PagamentoPendenteEvent(pedidoId, UUID.randomUUID(), "timeout", Instant.now());

        publisher.publicarPagamentoPendente(event);

        verify(kafkaTemplate).send("pagamento.pendente", pedidoId.toString(), event);
    }
}
