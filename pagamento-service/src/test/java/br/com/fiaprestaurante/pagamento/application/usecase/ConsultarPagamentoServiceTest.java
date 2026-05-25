package br.com.fiaprestaurante.pagamento.application.usecase;

import br.com.fiaprestaurante.pagamento.application.port.output.PagamentoRepository;
import br.com.fiaprestaurante.pagamento.domain.entity.Pagamento;
import br.com.fiaprestaurante.pagamento.domain.valueobject.StatusPagamento;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarPagamentoServiceTest {

    @Mock
    private PagamentoRepository pagamentoRepository;

    private ConsultarPagamentoService service;

    @BeforeEach
    void setUp() {
        service = new ConsultarPagamentoService(pagamentoRepository);
    }

    @Test
    void deveConsultarPorPedidoId() {
        UUID pedidoId = UUID.randomUUID();
        Pagamento pagamento = pagamento(pedidoId);
        when(pagamentoRepository.buscarPorPedidoId(pedidoId)).thenReturn(Optional.of(pagamento));

        var response = service.porPedidoId(pedidoId);

        assertThat(response).isPresent();
        assertThat(response.get().pedidoId()).isEqualTo(pedidoId);
        assertThat(response.get().status()).isEqualTo("PENDENTE");
    }

    @Test
    void deveRetornarVazioQuandoNaoExistePagamento() {
        UUID pedidoId = UUID.randomUUID();
        when(pagamentoRepository.buscarPorPedidoId(pedidoId)).thenReturn(Optional.empty());

        assertThat(service.porPedidoId(pedidoId)).isEmpty();
    }

    @Test
    void deveListarPendentes() {
        Pagamento pagamento = pagamento(UUID.randomUUID());
        when(pagamentoRepository.listarPendentes(Integer.MAX_VALUE)).thenReturn(List.of(pagamento));

        var response = service.pendentes();

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().id()).isEqualTo(pagamento.getId());
    }

    private static Pagamento pagamento(UUID pedidoId) {
        return new Pagamento(UUID.randomUUID(), pedidoId, new BigDecimal("30.00"), StatusPagamento.PENDENTE, 1, "falha",
                Instant.parse("2026-05-21T10:00:00Z"), Instant.parse("2026-05-21T10:01:00Z"));
    }
}
