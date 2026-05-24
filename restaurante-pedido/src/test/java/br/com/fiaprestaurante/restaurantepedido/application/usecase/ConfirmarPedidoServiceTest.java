package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoCriadoEvent;
import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResponse;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoEventPublisher;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepository;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import br.com.fiaprestaurante.restaurantepedido.domain.exception.PedidoNaoEncontradoException;
import br.com.fiaprestaurante.shared.exception.BusinessException;
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
 * Testes unitarios do {@link ConfirmarPedidoService} - mocka repositorio e
 * publisher e verifica orquestracao, idempotencia e validacao de propriedade.
 *
 * @author Danilo Fernando
 */
class ConfirmarPedidoServiceTest {

    private static final UUID CLIENTE_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID OUTRO_CLIENTE_ID = UUID.fromString("99999999-9999-4999-8999-999999999999");
    private static final UUID RESTAURANTE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

    private PedidoRepository pedidoRepository;
    private PedidoEventPublisher eventPublisher;
    private ConfirmarPedidoService service;

    @BeforeEach
    void setUp() {
        pedidoRepository = mock(PedidoRepository.class);
        eventPublisher = mock(PedidoEventPublisher.class);
        service = new ConfirmarPedidoService(pedidoRepository, eventPublisher);
    }

    private Pedido pedidoCriado() {
        return new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(
                new ItemPedido(PRODUTO_ID, "X-Burger", 2, new BigDecimal("25.90"))));
    }

    @Test
    void deveConfirmarPedidoEPublicarEventoNoKafka() {
        Pedido pedido = pedidoCriado();
        when(pedidoRepository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));
        when(pedidoRepository.salvar(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        PedidoResponse resposta = service.executar(pedido.getId(), CLIENTE_ID);

        assertThat(resposta.status()).isEqualTo("CONFIRMADO");

        ArgumentCaptor<PedidoCriadoEvent> eventCaptor = ArgumentCaptor.forClass(PedidoCriadoEvent.class);
        verify(eventPublisher).publicarPedidoCriado(eventCaptor.capture());
        PedidoCriadoEvent evento = eventCaptor.getValue();
        assertThat(evento.pedidoId()).isEqualTo(pedido.getId());
        assertThat(evento.clienteId()).isEqualTo(CLIENTE_ID);
        assertThat(evento.valorTotal()).isEqualByComparingTo("51.80");
        assertThat(evento.timestamp()).isNotNull();
    }

    @Test
    void deveLancarPedidoNaoEncontradoSeIdInexistente() {
        UUID idInexistente = UUID.randomUUID();
        when(pedidoRepository.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.executar(idInexistente, CLIENTE_ID))
                .isInstanceOf(PedidoNaoEncontradoException.class);

        verify(eventPublisher, never()).publicarPedidoCriado(any());
    }

    @Test
    void deveRecusarConfirmacaoDePedidoDeOutroCliente() {
        Pedido pedido = pedidoCriado();
        when(pedidoRepository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> service.executar(pedido.getId(), OUTRO_CLIENTE_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pertence");

        verify(eventPublisher, never()).publicarPedidoCriado(any());
    }
}
