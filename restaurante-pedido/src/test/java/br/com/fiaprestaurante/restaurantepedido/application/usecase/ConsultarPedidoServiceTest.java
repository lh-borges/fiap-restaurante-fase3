package br.com.fiaprestaurante.restaurantepedido.application.usecase;

import br.com.fiaprestaurante.restaurantepedido.application.dto.PedidoResponse;
import br.com.fiaprestaurante.restaurantepedido.application.port.output.PedidoRepository;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.ItemPedido;
import br.com.fiaprestaurante.restaurantepedido.domain.entity.Pedido;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes unitarios do {@link ConsultarPedidoService} - cobre busca por
 * ID e listagem por cliente, mockando o repositorio.
 *
 * @author Danilo Fernando
 */
class ConsultarPedidoServiceTest {

    private static final UUID CLIENTE_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID RESTAURANTE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

    private PedidoRepository pedidoRepository;
    private ConsultarPedidoService service;

    @BeforeEach
    void setUp() {
        pedidoRepository = mock(PedidoRepository.class);
        service = new ConsultarPedidoService(pedidoRepository);
    }

    private Pedido novoPedido() {
        return new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(
                new ItemPedido(PRODUTO_ID, "X-Burger", 1, new BigDecimal("25.90"))));
    }

    @Test
    void porIdDeveRetornarPedidoQuandoExistir() {
        Pedido pedido = novoPedido();
        when(pedidoRepository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));

        Optional<PedidoResponse> resposta = service.porId(pedido.getId());

        assertThat(resposta).isPresent();
        assertThat(resposta.get().id()).isEqualTo(pedido.getId());
        assertThat(resposta.get().status()).isEqualTo("CRIADO");
    }

    @Test
    void porIdDeveRetornarVazioQuandoNaoEncontrado() {
        UUID id = UUID.randomUUID();
        when(pedidoRepository.buscarPorId(id)).thenReturn(Optional.empty());

        Optional<PedidoResponse> resposta = service.porId(id);

        assertThat(resposta).isEmpty();
    }

    @Test
    void porClienteDeveRetornarListaConvertida() {
        when(pedidoRepository.listarPorCliente(CLIENTE_ID))
                .thenReturn(List.of(novoPedido(), novoPedido()));

        List<PedidoResponse> resposta = service.porCliente(CLIENTE_ID);

        assertThat(resposta).hasSize(2);
        assertThat(resposta).allMatch(p -> p.clienteId().equals(CLIENTE_ID));
    }

    @Test
    void porClienteDeveRetornarVazioQuandoSemPedidos() {
        when(pedidoRepository.listarPorCliente(CLIENTE_ID)).thenReturn(List.of());

        List<PedidoResponse> resposta = service.porCliente(CLIENTE_ID);

        assertThat(resposta).isEmpty();
    }
}
