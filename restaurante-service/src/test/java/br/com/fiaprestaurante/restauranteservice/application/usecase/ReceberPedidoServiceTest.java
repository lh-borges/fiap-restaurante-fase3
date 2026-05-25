package br.com.fiaprestaurante.restauranteservice.application.usecase;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoProntoParaCozinhaEvent;
import br.com.fiaprestaurante.restauranteservice.application.port.output.PedidoCozinhaRepository;
import br.com.fiaprestaurante.restauranteservice.domain.entity.PedidoCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link ReceberPedidoService}.
 *
 * @author Danilo Fernando
 */
class ReceberPedidoServiceTest {

    private static final UUID PEDIDO_ID = UUID.randomUUID();
    private static final UUID RESTAURANTE_ID = UUID.randomUUID();
    private static final UUID PRODUTO_ID = UUID.randomUUID();

    private PedidoCozinhaRepository repository;
    private ReceberPedidoService service;

    @BeforeEach
    void setUp() {
        repository = mock(PedidoCozinhaRepository.class);
        when(repository.salvar(any(PedidoCozinha.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new ReceberPedidoService(repository);
    }

    @Test
    void deveCriarPedidoCozinhaNoStatusRecebido() {
        PedidoProntoParaCozinhaEvent event = new PedidoProntoParaCozinhaEvent(
                PEDIDO_ID,
                RESTAURANTE_ID,
                List.of(new PedidoProntoParaCozinhaEvent.Item(PRODUTO_ID, "X-Burger", 2)),
                Instant.now()
        );

        service.executar(event);

        ArgumentCaptor<PedidoCozinha> captor = ArgumentCaptor.forClass(PedidoCozinha.class);
        verify(repository).salvar(captor.capture());
        PedidoCozinha salvo = captor.getValue();
        assertThat(salvo.getPedidoId()).isEqualTo(PEDIDO_ID);
        assertThat(salvo.getRestauranteId()).isEqualTo(RESTAURANTE_ID);
        assertThat(salvo.getStatus()).isEqualTo(StatusCozinha.RECEBIDO);
        assertThat(salvo.getItens()).hasSize(1);
        assertThat(salvo.getItens().get(0).getNome()).isEqualTo("X-Burger");
        assertThat(salvo.getItens().get(0).getQuantidade()).isEqualTo(2);
    }
}
