package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.messaging;

import br.com.fiaprestaurante.restaurantepedido.application.port.input.AtualizarStatusPagamentoUseCase;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Testes unitarios do {@link PagamentoAprovadoConsumer} - cobre o
 * processamento de eventos validos, payloads invalidos (descartados),
 * pedido nao encontrado (descartado) e demais falhas (propagam).
 *
 * @author Danilo Fernando
 */
class PagamentoAprovadoConsumerTest {

    private static final UUID PEDIDO_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID PAGAMENTO_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");

    private AtualizarStatusPagamentoUseCase useCase;
    private PagamentoAprovadoConsumer consumer;

    @BeforeEach
    void setUp() {
        useCase = mock(AtualizarStatusPagamentoUseCase.class);
        consumer = new PagamentoAprovadoConsumer(useCase);
    }

    private Map<String, Object> eventoValido() {
        Map<String, Object> evento = new HashMap<>();
        evento.put("pedidoId", PEDIDO_ID.toString());
        evento.put("pagamentoId", PAGAMENTO_ID.toString());
        evento.put("timestamp", Instant.now().toString());
        return evento;
    }

    @Test
    void deveDelegarParaUseCaseComPedidoIdEPagamentoId() {
        consumer.consumir(eventoValido());

        ArgumentCaptor<UUID> pedidoCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UUID> pagamentoCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(useCase).marcarComoPago(pedidoCaptor.capture(), pagamentoCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(pedidoCaptor.getValue()).isEqualTo(PEDIDO_ID);
        org.assertj.core.api.Assertions.assertThat(pagamentoCaptor.getValue()).isEqualTo(PAGAMENTO_ID);
    }

    @Test
    void devePedidoIdInvalidoSerDescartado() {
        Map<String, Object> invalido = new HashMap<>();
        invalido.put("pedidoId", "nao-eh-uuid");
        invalido.put("pagamentoId", PAGAMENTO_ID.toString());

        consumer.consumir(invalido);

        verify(useCase, never()).marcarComoPago(any(), any());
    }

    @Test
    void devePedidoNaoEncontradoSerDescartado() {
        doThrow(new PedidoNaoEncontradoException("nao existe"))
                .when(useCase).marcarComoPago(eq(PEDIDO_ID), eq(PAGAMENTO_ID));

        // Nao deve propagar — consumer engole e loga
        consumer.consumir(eventoValido());

        verify(useCase).marcarComoPago(PEDIDO_ID, PAGAMENTO_ID);
    }

    @Test
    void deveErroInesperadoPropagar() {
        doThrow(new IllegalStateException("falha de banco"))
                .when(useCase).marcarComoPago(any(), any());

        assertThatThrownBy(() -> consumer.consumir(eventoValido()))
                .isInstanceOf(IllegalStateException.class);
    }
}
