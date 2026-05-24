package br.com.fiaprestaurante.restaurantepedido.adapter.inbound.messaging;

import br.com.fiaprestaurante.restaurantepedido.TestFixtures;
import br.com.fiaprestaurante.restaurantepedido.application.port.input.AtualizarStatusPagamentoUseCase;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PagamentoConsumerTest {

    @Mock
    private AtualizarStatusPagamentoUseCase atualizarStatusPagamento;

    @Test
    void deveConsumirPagamentoAprovadoValido() {
        PagamentoAprovadoConsumer consumer = new PagamentoAprovadoConsumer(atualizarStatusPagamento);

        consumer.consumir(Map.of(
                "pedidoId", TestFixtures.PEDIDO_ID.toString(),
                "pagamentoId", TestFixtures.PAGAMENTO_ID.toString()
        ));

        verify(atualizarStatusPagamento).marcarComoPago(TestFixtures.PEDIDO_ID, TestFixtures.PAGAMENTO_ID);
    }

    @Test
    void deveDescartarPagamentoAprovadoInvalidoOuPedidoInexistente() {
        PagamentoAprovadoConsumer consumer = new PagamentoAprovadoConsumer(atualizarStatusPagamento);
        consumer.consumir(Map.of("pedidoId", "invalido", "pagamentoId", TestFixtures.PAGAMENTO_ID.toString()));
        verify(atualizarStatusPagamento, never()).marcarComoPago(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        doThrow(new PedidoNaoEncontradoException("não encontrado"))
                .when(atualizarStatusPagamento).marcarComoPago(TestFixtures.PEDIDO_ID, TestFixtures.PAGAMENTO_ID);
        consumer.consumir(Map.of(
                "pedidoId", TestFixtures.PEDIDO_ID.toString(),
                "pagamentoId", TestFixtures.PAGAMENTO_ID.toString()
        ));
    }

    @Test
    void devePropagarErroInesperadoNoPagamentoAprovado() {
        PagamentoAprovadoConsumer consumer = new PagamentoAprovadoConsumer(atualizarStatusPagamento);
        doThrow(new IllegalStateException("falha"))
                .when(atualizarStatusPagamento).marcarComoPago(TestFixtures.PEDIDO_ID, TestFixtures.PAGAMENTO_ID);

        assertThatThrownBy(() -> consumer.consumir(Map.of(
                "pedidoId", TestFixtures.PEDIDO_ID.toString(),
                "pagamentoId", TestFixtures.PAGAMENTO_ID.toString()
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessage("falha");
    }

    @Test
    void deveConsumirPagamentoPendenteValidoEComMotivoPadrao() {
        PagamentoPendenteConsumer consumer = new PagamentoPendenteConsumer(atualizarStatusPagamento);

        consumer.consumir(Map.of("pedidoId", TestFixtures.PEDIDO_ID.toString(), "motivo", "gateway fora"));
        consumer.consumir(Map.of("pedidoId", TestFixtures.PEDIDO_ID.toString()));

        verify(atualizarStatusPagamento).marcarComoPendente(TestFixtures.PEDIDO_ID, "gateway fora");
        verify(atualizarStatusPagamento).marcarComoPendente(TestFixtures.PEDIDO_ID, "gateway indisponível");
    }

    @Test
    void deveDescartarPagamentoPendenteInvalidoOuPedidoInexistente() {
        PagamentoPendenteConsumer consumer = new PagamentoPendenteConsumer(atualizarStatusPagamento);
        consumer.consumir(Map.of("pedidoId", "invalido"));
        verify(atualizarStatusPagamento, never()).marcarComoPendente(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        doThrow(new PedidoNaoEncontradoException("não encontrado"))
                .when(atualizarStatusPagamento).marcarComoPendente(TestFixtures.PEDIDO_ID, "gateway fora");
        consumer.consumir(Map.of("pedidoId", TestFixtures.PEDIDO_ID.toString(), "motivo", "gateway fora"));
    }

    @Test
    void devePropagarErroInesperadoNoPagamentoPendente() {
        PagamentoPendenteConsumer consumer = new PagamentoPendenteConsumer(atualizarStatusPagamento);
        doThrow(new IllegalStateException("falha"))
                .when(atualizarStatusPagamento).marcarComoPendente(TestFixtures.PEDIDO_ID, "gateway fora");

        assertThatThrownBy(() -> consumer.consumir(Map.of(
                "pedidoId", TestFixtures.PEDIDO_ID.toString(),
                "motivo", "gateway fora"
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessage("falha");
    }
}
