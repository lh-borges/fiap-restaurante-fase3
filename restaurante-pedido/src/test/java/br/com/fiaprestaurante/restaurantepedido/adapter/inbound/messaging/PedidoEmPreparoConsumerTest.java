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
 * Testes unitarios do {@link PedidoEmPreparoConsumer}.
 *
 * @author Danilo Fernando
 */
class PedidoEmPreparoConsumerTest {

    private AtualizarStatusCozinhaUseCase useCase;
    private PedidoEmPreparoConsumer consumer;

    @BeforeEach
    void setUp() {
        useCase = mock(AtualizarStatusCozinhaUseCase.class);
        consumer = new PedidoEmPreparoConsumer(useCase);
    }

    @Test
    void deveExtrairPedidoIdEDelegar() {
        UUID pedidoId = UUID.randomUUID();
        consumer.consumir(Map.of("pedidoId", pedidoId.toString()));
        verify(useCase).marcarComoEmPreparo(pedidoId);
    }

    @Test
    void deveDescartarPayloadInvalido() {
        consumer.consumir(Map.of("pedidoId", "nao-eh-uuid"));
        verify(useCase, Mockito.never()).marcarComoEmPreparo(any());
    }

    @Test
    void devePropagarErroInesperado() {
        UUID pedidoId = UUID.randomUUID();
        doThrow(new RuntimeException("kaboom")).when(useCase).marcarComoEmPreparo(pedidoId);
        try {
            consumer.consumir(Map.of("pedidoId", pedidoId.toString()));
        } catch (RuntimeException e) {
            // esperado
        }
    }

    @Test
    void deveDescartarPedidoNaoEncontrado() {
        UUID pedidoId = UUID.randomUUID();
        doThrow(new PedidoNaoEncontradoException("nao achei")).when(useCase).marcarComoEmPreparo(pedidoId);
        consumer.consumir(Map.of("pedidoId", pedidoId.toString()));
        // sem excecao propagada
    }
}
