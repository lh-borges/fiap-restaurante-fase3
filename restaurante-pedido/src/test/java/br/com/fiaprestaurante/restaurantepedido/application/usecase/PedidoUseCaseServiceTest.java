package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.TestFixtures;
import br.com.fiaprestaurante.restaurantepedido.application.dto.CriarPedidoCommand;
import br.com.fiaprestaurante.restaurantepedido.application.dto.ItemPedidoCommand;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoCriadoEvent;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResponse;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoEventPublisher;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepository;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import br.com.fiaprestaurante.restaurantepedido.domain.valueobject.StatusPedido;
import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoUseCaseServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoEventPublisher eventPublisher;

    @Test
    void deveCriarPedidoCalculandoTotal() {
        CriarPedidoService service = new CriarPedidoService(pedidoRepository);
        when(pedidoRepository.salvar(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PedidoResponse response = service.executar(new CriarPedidoCommand(
                TestFixtures.CLIENTE_ID,
                TestFixtures.RESTAURANTE_ID,
                List.of(new ItemPedidoCommand(TestFixtures.PRODUTO_ID, "Pizza", 2, new BigDecimal("25.50")))
        ));

        assertThat(response.clienteId()).isEqualTo(TestFixtures.CLIENTE_ID);
        assertThat(response.restauranteId()).isEqualTo(TestFixtures.RESTAURANTE_ID);
        assertThat(response.status()).isEqualTo("CRIADO");
        assertThat(response.valorTotal()).isEqualByComparingTo("51.00");
        verify(pedidoRepository).salvar(any(Pedido.class));
    }

    @Test
    void deveConsultarPedidoPorIdEPorCliente() {
        ConsultarPedidoService service = new ConsultarPedidoService(pedidoRepository);
        Pedido pedido = TestFixtures.pedidoCriado();
        when(pedidoRepository.buscarPorId(TestFixtures.PEDIDO_ID)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.listarPorCliente(TestFixtures.CLIENTE_ID)).thenReturn(List.of(pedido));

        assertThat(service.porId(TestFixtures.PEDIDO_ID, TestFixtures.CLIENTE_ID)).get()
                .extracting(PedidoResponse::id)
                .isEqualTo(TestFixtures.PEDIDO_ID);
        assertThat(service.porCliente(TestFixtures.CLIENTE_ID)).singleElement()
                .extracting(PedidoResponse::clienteId)
                .isEqualTo(TestFixtures.CLIENTE_ID);
    }

    @Test
    void deveConfirmarPedidoDoClienteEPublicarEvento() {
        ConfirmarPedidoService service = new ConfirmarPedidoService(pedidoRepository, eventPublisher);
        Pedido pedido = TestFixtures.pedidoCriado();
        when(pedidoRepository.buscarPorId(TestFixtures.PEDIDO_ID)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.salvar(pedido)).thenReturn(pedido);

        PedidoResponse response = service.executar(TestFixtures.PEDIDO_ID, TestFixtures.CLIENTE_ID);

        assertThat(response.status()).isEqualTo("CONFIRMADO");
        ArgumentCaptor<PedidoCriadoEvent> eventCaptor = ArgumentCaptor.forClass(PedidoCriadoEvent.class);
        verify(eventPublisher).publicarPedidoCriado(eventCaptor.capture());
        assertThat(eventCaptor.getValue().pedidoId()).isEqualTo(TestFixtures.PEDIDO_ID);
        assertThat(eventCaptor.getValue().clienteId()).isEqualTo(TestFixtures.CLIENTE_ID);
        assertThat(eventCaptor.getValue().valorTotal()).isEqualByComparingTo("51.00");
        assertThat(eventCaptor.getValue().timestamp()).isNotNull();
    }

    @Test
    void naoDeveConfirmarPedidoDeOutroCliente() {
        ConfirmarPedidoService service = new ConfirmarPedidoService(pedidoRepository, eventPublisher);
        when(pedidoRepository.buscarPorId(TestFixtures.PEDIDO_ID)).thenReturn(Optional.of(TestFixtures.pedidoCriado()));

        assertThatThrownBy(() -> service.executar(TestFixtures.PEDIDO_ID, TestFixtures.OUTRO_CLIENTE_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage("pedido não pertence ao cliente autenticado");
        verify(pedidoRepository, never()).salvar(any());
        verify(eventPublisher, never()).publicarPedidoCriado(any());
    }

    @Test
    void deveFalharAoConfirmarPedidoInexistente() {
        ConfirmarPedidoService service = new ConfirmarPedidoService(pedidoRepository, eventPublisher);
        when(pedidoRepository.buscarPorId(TestFixtures.PEDIDO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(TestFixtures.PEDIDO_ID, TestFixtures.CLIENTE_ID))
                .isInstanceOf(PedidoNaoEncontradoException.class)
                .hasMessageContaining(TestFixtures.PEDIDO_ID.toString());
    }

    @Test
    void deveAtualizarStatusPagamentoParaPagoEPendente() {
        AtualizarStatusPagamentoService service = new AtualizarStatusPagamentoService(pedidoRepository, eventPublisher);
        Pedido pago = TestFixtures.pedidoConfirmado();
        Pedido pendente = TestFixtures.pedidoConfirmado();
        when(pedidoRepository.buscarPorId(TestFixtures.PEDIDO_ID))
                .thenReturn(Optional.of(pago))
                .thenReturn(Optional.of(pendente));

        service.marcarComoPago(TestFixtures.PEDIDO_ID, TestFixtures.PAGAMENTO_ID);
        service.marcarComoPendente(TestFixtures.PEDIDO_ID, "gateway fora");

        assertThat(pago.getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(pago.getPagamentoId()).isEqualTo(TestFixtures.PAGAMENTO_ID);
        assertThat(pendente.getStatus()).isEqualTo(StatusPedido.PENDENTE_PAGAMENTO);
        assertThat(pendente.getMotivoPendencia()).isEqualTo("gateway fora");
        verify(pedidoRepository).salvar(pago);
        verify(pedidoRepository).salvar(pendente);
    }

    @Test
    void deveFalharAoAtualizarPagamentoDePedidoInexistente() {
        AtualizarStatusPagamentoService service = new AtualizarStatusPagamentoService(pedidoRepository, eventPublisher);
        when(pedidoRepository.buscarPorId(TestFixtures.PEDIDO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.marcarComoPago(TestFixtures.PEDIDO_ID, TestFixtures.PAGAMENTO_ID))
                .isInstanceOf(PedidoNaoEncontradoException.class)
                .hasMessageContaining(TestFixtures.PEDIDO_ID.toString());
        verify(pedidoRepository, never()).salvar(any());
    }

    @Test
    void deveRetornarStatusEstaticoDoModulo() {
        ConsultarModuloRestaurantePedidoService service = new ConsultarModuloRestaurantePedidoService();

        assertThat(service.executar())
                .contains("restaurante/pedido operacional")
                .contains("Kafka");
    }
}
