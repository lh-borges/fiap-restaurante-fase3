package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepository;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import br.com.fiaprestaurante.restaurantepedido.domain.valueobject.StatusPedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
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
 * Testes unitarios do {@link AtualizarStatusCozinhaService} — cobre as
 * transicoes EM_PREPARO e PRONTO disparadas pelos eventos Kafka do
 * restaurante-service.
 *
 * @author Danilo Fernando
 */
class AtualizarStatusCozinhaServiceTest {

    private static final UUID CLIENTE_ID = UUID.randomUUID();
    private static final UUID RESTAURANTE_ID = UUID.randomUUID();
    private static final UUID PRODUTO_ID = UUID.randomUUID();
    private static final UUID PAGAMENTO_ID = UUID.randomUUID();

    private PedidoRepository repository;
    private AtualizarStatusCozinhaService service;

    @BeforeEach
    void setUp() {
        repository = mock(PedidoRepository.class);
        when(repository.salvar(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new AtualizarStatusCozinhaService(repository);
    }

    private Pedido pedidoPago() {
        Pedido p = new Pedido(CLIENTE_ID, RESTAURANTE_ID,
                List.of(new ItemPedido(PRODUTO_ID, "X", 1, new BigDecimal("10"))));
        p.confirmar();
        p.marcarComoPago(PAGAMENTO_ID);
        return p;
    }

    @Test
    void deveMarcarComoEmPreparo() {
        Pedido pedido = pedidoPago();
        when(repository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));

        service.marcarComoEmPreparo(pedido.getId());

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(repository).salvar(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusPedido.EM_PREPARO);
    }

    @Test
    void deveMarcarComoPronto() {
        Pedido pedido = pedidoPago();
        pedido.marcarComoEmPreparo();
        when(repository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));

        service.marcarComoPronto(pedido.getId());

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(repository).salvar(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusPedido.PRONTO);
    }

    @Test
    void deveLancarSePedidoNaoEncontradoEmEmPreparo() {
        UUID id = UUID.randomUUID();
        when(repository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.marcarComoEmPreparo(id))
                .isInstanceOf(PedidoNaoEncontradoException.class);
        verify(repository, never()).salvar(any());
    }

    @Test
    void deveLancarSePedidoNaoEncontradoEmPronto() {
        UUID id = UUID.randomUUID();
        when(repository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.marcarComoPronto(id))
                .isInstanceOf(PedidoNaoEncontradoException.class);
        verify(repository, never()).salvar(any());
    }
}
