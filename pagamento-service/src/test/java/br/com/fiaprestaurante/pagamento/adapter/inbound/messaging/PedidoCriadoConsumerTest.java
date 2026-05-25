package br.com.fiaprestaurante.pagamento.adapter.inbound.messaging;

import br.com.fiaprestaurante.pagamento.application.dto.PagamentoResponse;
import br.com.fiaprestaurante.pagamento.application.dto.ProcessarPagamentoCommand;
import br.com.fiaprestaurante.pagamento.application.port.input.ProcessarPagamentoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link PedidoCriadoConsumer} - cobre payload
 * valido, descarte de payload invalido, propagacao de falha tecnica.
 *
 * @author Danilo Fernando
 */
class PedidoCriadoConsumerTest {

    private static final UUID PEDIDO_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");

    private ProcessarPagamentoUseCase useCase;
    private PedidoCriadoConsumer consumer;

    @BeforeEach
    void setUp() {
        useCase = mock(ProcessarPagamentoUseCase.class);
        consumer = new PedidoCriadoConsumer(useCase);
    }

    private Map<String, Object> eventoValido() {
        Map<String, Object> e = new HashMap<>();
        e.put("pedidoId", PEDIDO_ID.toString());
        e.put("valorTotal", 59.30);
        e.put("timestamp", Instant.now().toString());
        return e;
    }

    @Test
    void deveExtrairPedidoIdEValorTotalEDelegarAoUseCase() {
        when(useCase.executar(any(ProcessarPagamentoCommand.class)))
                .thenReturn(new PagamentoResponse(UUID.randomUUID(), PEDIDO_ID,
                        new BigDecimal("59.30"), "APROVADO", 1, null,
                        Instant.now(), Instant.now()));

        consumer.consumir(eventoValido());

        ArgumentCaptor<ProcessarPagamentoCommand> captor =
                ArgumentCaptor.forClass(ProcessarPagamentoCommand.class);
        verify(useCase).executar(captor.capture());
        assertThat(captor.getValue().pedidoId()).isEqualTo(PEDIDO_ID);
        assertThat(captor.getValue().valorTotal()).isEqualByComparingTo("59.30");
    }

    @Test
    void payloadInvalidoDeveSerDescartadoSemPropagar() {
        Map<String, Object> ruim = new HashMap<>();
        ruim.put("pedidoId", "nao-eh-uuid");
        ruim.put("valorTotal", 10);

        consumer.consumir(ruim);

        verify(useCase, never()).executar(any());
    }

    @Test
    void faltaDePedidoIdDeveSerDescartado() {
        Map<String, Object> ruim = new HashMap<>();
        ruim.put("valorTotal", 10);

        consumer.consumir(ruim);

        verify(useCase, never()).executar(any());
    }

    @Test
    void falhaTecnicaDoUseCaseDevePropagar() {
        when(useCase.executar(any(ProcessarPagamentoCommand.class)))
                .thenThrow(new RuntimeException("erro de banco"));

        assertThatThrownBy(() -> consumer.consumir(eventoValido()))
                .isInstanceOf(RuntimeException.class);
    }
}
