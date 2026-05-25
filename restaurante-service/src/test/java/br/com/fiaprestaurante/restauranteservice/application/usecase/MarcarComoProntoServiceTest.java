package br.com.fiaprestaurante.restauranteservice.application.usecase;

import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoCozinhaResponse;
import br.com.fiaprestaurante.restauranteservice.application.dto.PedidoProntoEvent;
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
 * Testes unitarios do {@link MarcarComoProntoService}.
 *
 * @author Danilo Fernando
 */
class MarcarComoProntoServiceTest {

    private PedidoCozinhaRepository repository;
    private CozinhaEventPublisher publisher;
    private MarcarComoProntoService service;

    @BeforeEach
    void setUp() {
        repository = mock(PedidoCozinhaRepository.class);
        publisher = mock(CozinhaEventPublisher.class);
        when(repository.salvar(any(PedidoCozinha.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new MarcarComoProntoService(repository, publisher);
    }

    private PedidoCozinha pedidoEmPreparo() {
        PedidoCozinha p = new PedidoCozinha(UUID.randomUUID(), UUID.randomUUID(),
                List.of(new ItemCozinha(UUID.randomUUID(), "X-Burger", 1)));
        p.iniciarPreparo();
        return p;
    }

    @Test
    void deveTransitarParaProntoEPublicarEvento() {
        PedidoCozinha pedido = pedidoEmPreparo();
        when(repository.porId(pedido.getId())).thenReturn(Optional.of(pedido));

        PedidoCozinhaResponse response = service.executar(pedido.getId());

        assertThat(response.status()).isEqualTo(StatusCozinha.PRONTO.name());
        ArgumentCaptor<PedidoProntoEvent> evtCaptor = ArgumentCaptor.forClass(PedidoProntoEvent.class);
        verify(publisher).publicarPronto(evtCaptor.capture());
        assertThat(evtCaptor.getValue().pedidoId()).isEqualTo(pedido.getPedidoId());
        assertThat(evtCaptor.getValue().pedidoCozinhaId()).isEqualTo(pedido.getId());
    }

    @Test
    void deveLancarSePedidoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(repository.porId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(id))
                .isInstanceOf(PedidoCozinhaNaoEncontradoException.class);

        verify(publisher, never()).publicarPronto(any());
    }
}
