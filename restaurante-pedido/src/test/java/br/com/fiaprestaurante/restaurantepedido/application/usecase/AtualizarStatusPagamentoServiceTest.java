package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoProntoParaCozinhaEvent;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoEventPublisher;
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
 * Testes unitarios do {@link AtualizarStatusPagamentoService} - cobre as
 * transicoes para PAGO e PENDENTE_PAGAMENTO disparadas por eventos Kafka
 * do servico de pagamento, alem do publish de {@code pedido.pronto-para-cozinha}
 * apos a aprovacao.
 *
 * @author Danilo Fernando
 */
class AtualizarStatusPagamentoServiceTest {

    private static final UUID CLIENTE_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID RESTAURANTE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID PAGAMENTO_ID = UUID.fromString("33333333-3333-4333-8333-333333333333");

    private PedidoRepository pedidoRepository;
    private PedidoEventPublisher eventPublisher;
    private AtualizarStatusPagamentoService service;

    @BeforeEach
    void setUp() {
        pedidoRepository = mock(PedidoRepository.class);
        eventPublisher = mock(PedidoEventPublisher.class);
        service = new AtualizarStatusPagamentoService(pedidoRepository, eventPublisher);
    }

    private Pedido pedidoConfirmado() {
        Pedido p = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(
                new ItemPedido(PRODUTO_ID, "X-Burger", 1, new BigDecimal("25.90"))));
        p.confirmar();
        return p;
    }

    @Test
    void deveMarcarPedidoComoPagoEPreservarPagamentoId() {
        Pedido pedido = pedidoConfirmado();
        when(pedidoRepository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));
        when(pedidoRepository.salvar(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        service.marcarComoPago(pedido.getId(), PAGAMENTO_ID);

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).salvar(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(captor.getValue().getPagamentoId()).isEqualTo(PAGAMENTO_ID);
    }

    @Test
    void deveMarcarPedidoComoPendentePagamentoComMotivo() {
        Pedido pedido = pedidoConfirmado();
        when(pedidoRepository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));
        when(pedidoRepository.salvar(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        service.marcarComoPendente(pedido.getId(), "gateway fora");

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).salvar(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusPedido.PENDENTE_PAGAMENTO);
        assertThat(captor.getValue().getMotivoPendencia()).isEqualTo("gateway fora");
        verify(eventPublisher, never()).publicarProntoParaCozinha(any());
    }

    @Test
    void marcarComoPagoDeveLancarSePedidoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(pedidoRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.marcarComoPago(id, PAGAMENTO_ID))
                .isInstanceOf(PedidoNaoEncontradoException.class);

        verify(pedidoRepository, never()).salvar(any());
        verify(eventPublisher, never()).publicarProntoParaCozinha(any());
    }

    @Test
    void marcarComoPendenteDeveLancarSePedidoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(pedidoRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.marcarComoPendente(id, "motivo"))
                .isInstanceOf(PedidoNaoEncontradoException.class);

        verify(pedidoRepository, never()).salvar(any());
    }

    @Test
    void deveNotificarCozinhaAposAprovacao() {
        Pedido pedido = pedidoConfirmado();
        when(pedidoRepository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));
        when(pedidoRepository.salvar(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        service.marcarComoPago(pedido.getId(), PAGAMENTO_ID);

        ArgumentCaptor<PedidoProntoParaCozinhaEvent> captor =
                ArgumentCaptor.forClass(PedidoProntoParaCozinhaEvent.class);
        verify(eventPublisher).publicarProntoParaCozinha(captor.capture());
        PedidoProntoParaCozinhaEvent event = captor.getValue();
        assertThat(event.pedidoId()).isEqualTo(pedido.getId());
        assertThat(event.restauranteId()).isEqualTo(RESTAURANTE_ID);
        assertThat(event.itens()).hasSize(1);
        assertThat(event.itens().get(0).nome()).isEqualTo("X-Burger");
        assertThat(event.itens().get(0).quantidade()).isEqualTo(1);
    }
}
