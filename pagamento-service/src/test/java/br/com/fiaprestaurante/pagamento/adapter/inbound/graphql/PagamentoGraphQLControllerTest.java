package br.com.fiaprestaurante.pagamento.adapter.inbound.graphql;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoResponse;
import br.com.fiaprestaurante.pagamento.application.port.input.ConsultarPagamentoUseCase;
import br.com.fiaprestaurante.pagamento.domain.exception.PagamentoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentoGraphQLControllerTest {

    @Mock
    private ConsultarPagamentoUseCase consultarPagamento;

    private PagamentoGraphQLController controller;

    @BeforeEach
    void setUp() {
        controller = new PagamentoGraphQLController(consultarPagamento);
    }

    @Test
    void deveConsultarPagamentoPorPedido() {
        UUID pedidoId = UUID.randomUUID();
        PagamentoResponse response = response(pedidoId);
        when(consultarPagamento.porPedidoId(pedidoId)).thenReturn(Optional.of(response));

        PagamentoResponse resultado = controller.pagamentoPorPedido(pedidoId.toString());

        assertThat(resultado).isEqualTo(response);
    }

    @Test
    void deveLancarNotFoundQuandoPagamentoNaoExiste() {
        UUID pedidoId = UUID.randomUUID();
        when(consultarPagamento.porPedidoId(pedidoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.pagamentoPorPedido(pedidoId.toString()))
                .isInstanceOf(PagamentoNaoEncontradoException.class)
                .hasMessage("Pagamento não encontrado para o pedido informado.");
    }

    @Test
    void deveListarPagamentosPendentes() {
        PagamentoResponse response = response(UUID.randomUUID());
        when(consultarPagamento.pendentes()).thenReturn(List.of(response));

        assertThat(controller.pagamentosPendentes()).containsExactly(response);
    }

    private static PagamentoResponse response(UUID pedidoId) {
        return new PagamentoResponse(UUID.randomUUID(), pedidoId, new BigDecimal("35.50"), "PENDENTE", 1, "timeout",
                Instant.parse("2026-05-21T10:00:00Z"), Instant.parse("2026-05-21T10:01:00Z"));
    }
}
