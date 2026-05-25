package br.com.fiaprestaurante.restauranteservice.adapter.inbound.messaging;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoProntoParaCozinhaEvent;
import br.com.fiaprestaurante.restauranteservice.application.port.input.ReceberPedidoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Testes unitarios do {@link PedidoProntoParaCozinhaConsumer}.
 *
 * @author Danilo Fernando
 */
class PedidoProntoParaCozinhaConsumerTest {

    private ReceberPedidoUseCase useCase;
    private PedidoProntoParaCozinhaConsumer consumer;

    @BeforeEach
    void setUp() {
        useCase = mock(ReceberPedidoUseCase.class);
        consumer = new PedidoProntoParaCozinhaConsumer(useCase);
    }

    @Test
    void devePropagarPayloadValido() {
        UUID pedidoId = UUID.randomUUID();
        UUID restauranteId = UUID.randomUUID();
        UUID produtoId = UUID.randomUUID();
        Map<String, Object> payload = Map.of(
                "pedidoId", pedidoId.toString(),
                "restauranteId", restauranteId.toString(),
                "itens", List.of(Map.of(
                        "produtoId", produtoId.toString(),
                        "nome", "X-Burger",
                        "quantidade", 2)),
                "timestamp", "2026-05-25T12:00:00Z"
        );

        consumer.consumir(payload);

        ArgumentCaptor<PedidoProntoParaCozinhaEvent> captor = ArgumentCaptor.forClass(PedidoProntoParaCozinhaEvent.class);
        verify(useCase).executar(captor.capture());
        PedidoProntoParaCozinhaEvent event = captor.getValue();
        assertThat(event.pedidoId()).isEqualTo(pedidoId);
        assertThat(event.restauranteId()).isEqualTo(restauranteId);
        assertThat(event.itens()).hasSize(1);
        assertThat(event.itens().get(0).nome()).isEqualTo("X-Burger");
    }

    @Test
    void deveDescartarPayloadComCampoFaltando() {
        Map<String, Object> incompleto = Map.of("pedidoId", UUID.randomUUID().toString());
        consumer.consumir(incompleto);
        verify(useCase, never()).executar(any());
    }

    @Test
    void deveDescartarPayloadComUuidInvalido() {
        Map<String, Object> ruim = Map.of(
                "pedidoId", "nao-eh-uuid",
                "restauranteId", UUID.randomUUID().toString(),
                "itens", List.of(),
                "timestamp", "2026-05-25T12:00:00Z"
        );
        consumer.consumir(ruim);
        verify(useCase, never()).executar(any());
    }
}
