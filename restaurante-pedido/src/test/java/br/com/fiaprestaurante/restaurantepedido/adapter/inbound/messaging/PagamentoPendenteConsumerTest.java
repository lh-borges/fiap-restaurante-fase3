package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.messaging;

import br.com.fiaprestaurante.restaurantepedido.application.port.input.AtualizarStatusPagamentoUseCase;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
 * Testes unitarios do {@link PagamentoPendenteConsumer} - cobre o
 * processamento de eventos validos, payloads invalidos (descartados),
 * pedido nao encontrado (descartado) e demais falhas (propagam).
 *
 * @author Danilo Fernando
 */
class PagamentoPendenteConsumerTest {

    private static final UUID PEDIDO_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID PAGAMENTO_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");

    private AtualizarStatusPagamentoUseCase useCase;
    private PagamentoPendenteConsumer consumer;

    @BeforeEach
    void setUp() {
        useCase = mock(AtualizarStatusPagamentoUseCase.class);
        consumer = new PagamentoPendenteConsumer(useCase);
    }

    private Map<String, Object> eventoValido(String motivo) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("pedidoId", PEDIDO_ID.toString());
        evento.put("pagamentoId", PAGAMENTO_ID.toString());
        evento.put("motivo", motivo);
        evento.put("timestamp", Instant.now().toString());
        return evento;
    }

    @Test
    void deveDelegarParaUseCaseComPedidoIdEMotivo() {
        consumer.consumir(eventoValido("gateway fora"));

        verify(useCase).marcarComoPendente(PEDIDO_ID, "gateway fora");
    }

    @Test
    void deveUsarMotivoPadraoQuandoCampoAusente() {
        Map<String, Object> evento = new HashMap<>();
        evento.put("pedidoId", PEDIDO_ID.toString());
        evento.put("pagamentoId", PAGAMENTO_ID.toString());

        consumer.consumir(evento);

        verify(useCase).marcarComoPendente(eq(PEDIDO_ID), eq("gateway indisponível"));
    }

    @Test
    void devePedidoIdInvalidoSerDescartado() {
        Map<String, Object> invalido = new HashMap<>();
        invalido.put("pedidoId", "nao-eh-uuid");
        invalido.put("motivo", "qualquer");

        consumer.consumir(invalido);

        verify(useCase, never()).marcarComoPendente(any(), any());
    }

    @Test
    void devePedidoNaoEncontradoSerDescartado() {
        doThrow(new PedidoNaoEncontradoException("nao existe"))
                .when(useCase).marcarComoPendente(eq(PEDIDO_ID), any());

        consumer.consumir(eventoValido("motivo"));

        verify(useCase).marcarComoPendente(eq(PEDIDO_ID), any());
    }

    @Test
    void deveErroInesperadoPropagar() {
        doThrow(new IllegalStateException("falha de banco"))
                .when(useCase).marcarComoPendente(any(), any());

        assertThatThrownBy(() -> consumer.consumir(eventoValido("motivo")))
                .isInstanceOf(IllegalStateException.class);
    }
}
