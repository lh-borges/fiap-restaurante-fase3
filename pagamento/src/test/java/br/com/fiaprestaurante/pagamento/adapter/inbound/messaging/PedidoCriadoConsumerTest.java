package br.com.fiaprestaurante.pagamento.adapter.inbound.messaging;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoResponse;
import br.com.fiaprestaurante.pagamento.application.dto.ProcessarPagamentoCommand;
import br.com.fiaprestaurante.pagamento.application.port.input.ProcessarPagamentoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoCriadoConsumerTest {

    @Mock
    private ProcessarPagamentoUseCase processarPagamento;

    private PedidoCriadoConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PedidoCriadoConsumer(processarPagamento);
    }

    @Test
    void deveProcessarPayloadValido() {
        UUID pedidoId = UUID.randomUUID();
        when(processarPagamento.executar(new ProcessarPagamentoCommand(pedidoId, new BigDecimal("35.50"))))
                .thenReturn(response(pedidoId));

        consumer.consumir(Map.of("pedidoId", pedidoId.toString(), "valorTotal", "35.50"));

        ArgumentCaptor<ProcessarPagamentoCommand> captor = ArgumentCaptor.forClass(ProcessarPagamentoCommand.class);
        verify(processarPagamento).executar(captor.capture());
        assertThat(captor.getValue().pedidoId()).isEqualTo(pedidoId);
        assertThat(captor.getValue().valorTotal()).isEqualByComparingTo("35.50");
    }

    @Test
    void deveDescartarPayloadInvalido() {
        consumer.consumir(Map.of("pedidoId", "uuid-invalido", "valorTotal", "35.50"));

        verifyNoInteractions(processarPagamento);
    }

    @Test
    void devePropagarErroDoUseCase() {
        UUID pedidoId = UUID.randomUUID();
        when(processarPagamento.executar(new ProcessarPagamentoCommand(pedidoId, new BigDecimal("35.50"))))
                .thenThrow(new IllegalStateException("falha de persistencia"));

        assertThatThrownBy(() -> consumer.consumir(Map.of("pedidoId", pedidoId.toString(), "valorTotal", "35.50")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("falha de persistencia");
    }

    private static PagamentoResponse response(UUID pedidoId) {
        return new PagamentoResponse(UUID.randomUUID(), pedidoId, new BigDecimal("35.50"), "APROVADO", 1, null,
                Instant.parse("2026-05-21T10:00:00Z"), Instant.parse("2026-05-21T10:01:00Z"));
    }
}
