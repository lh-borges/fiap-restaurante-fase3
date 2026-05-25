package br.com.fiaprestaurante.restauranteservice.application.usecase;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoCozinhaResponse;
import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoEmPreparoEvent;
import br.com.fiaprestaurante.restauranteservice.application.port.output.CozinhaEventPublisher;
import br.com.fiaprestaurante.restauranteservice.application.port.output.PedidoCozinhaRepository;
import br.com.fiaprestaurante.restauranteservice.domain.entity.ItemCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.entity.PedidoCozinha;
import br.com.fiaprestaurante.restauranteservice.domain.exception.PedidoCozinhaNaoEncontradoException;
import br.com.fiaprestaurante.restauranteservice.domain.valueobject.StatusCozinha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link IniciarPreparoService}.
 *
 * @author Danilo Fernando
 */
class IniciarPreparoServiceTest {

    private PedidoCozinhaRepository repository;
    private CozinhaEventPublisher publisher;
    private IniciarPreparoService service;

    @BeforeEach
    void setUp() {
        repository = mock(PedidoCozinhaRepository.class);
        publisher = mock(CozinhaEventPublisher.class);
        when(repository.salvar(any(PedidoCozinha.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new IniciarPreparoService(repository, publisher);
    }

    private PedidoCozinha pedidoRecebido() {
        return new PedidoCozinha(UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ItemCozinha(UUID.randomUUID(), "X-Burger", 1)));
    }

    @Test
    void deveTransitarParaEmPreparoEPublicarEvento() {
        PedidoCozinha pedido = pedidoRecebido();
        when(repository.porId(pedido.getId())).thenReturn(Optional.of(pedido));

        PedidoCozinhaResponse response = service.executar(pedido.getId());

        assertThat(response.status()).isEqualTo(StatusCozinha.EM_PREPARO.name());
        ArgumentCaptor<PedidoEmPreparoEvent> evtCaptor = ArgumentCaptor.forClass(PedidoEmPreparoEvent.class);
        verify(publisher).publicarEmPreparo(evtCaptor.capture());
        assertThat(evtCaptor.getValue().pedidoId()).isEqualTo(pedido.getPedidoId());
        assertThat(evtCaptor.getValue().pedidoCozinhaId()).isEqualTo(pedido.getId());
    }

    @Test
    void deveLancarSePedidoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(repository.porId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(id))
                .isInstanceOf(PedidoCozinhaNaoEncontradoException.class);

        verify(publisher, never()).publicarEmPreparo(any());
    }
}
