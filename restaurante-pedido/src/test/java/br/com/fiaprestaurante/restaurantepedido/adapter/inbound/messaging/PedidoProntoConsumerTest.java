package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.messaging;

import br.com.fiaprestaurante.restaurantepedido.application.port.input.AtualizarStatusCozinhaUseCase;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Testes unitarios do {@link PedidoProntoConsumer}.
 *
 * @author Danilo Fernando
 */
class PedidoProntoConsumerTest {

    private AtualizarStatusCozinhaUseCase useCase;
    private PedidoProntoConsumer consumer;

    @BeforeEach
    void setUp() {
        useCase = mock(AtualizarStatusCozinhaUseCase.class);
        consumer = new PedidoProntoConsumer(useCase);
    }

    @Test
    void deveExtrairPedidoIdEDelegar() {
        UUID pedidoId = UUID.randomUUID();
        consumer.consumir(Map.of("pedidoId", pedidoId.toString()));
        verify(useCase).marcarComoPronto(pedidoId);
    }

    @Test
    void deveDescartarPayloadInvalido() {
        consumer.consumir(Map.of("pedidoId", "nao-eh-uuid"));
        verify(useCase, Mockito.never()).marcarComoPronto(any());
    }

    @Test
    void deveDescartarPedidoNaoEncontrado() {
        UUID pedidoId = UUID.randomUUID();
        doThrow(new PedidoNaoEncontradoException("nao achei")).when(useCase).marcarComoPronto(pedidoId);
        consumer.consumir(Map.of("pedidoId", pedidoId.toString()));
    }
}
