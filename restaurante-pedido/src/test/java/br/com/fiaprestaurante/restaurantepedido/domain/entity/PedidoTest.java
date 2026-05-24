package br.com.fiaprestaurante.restaurantepedido.domain.entity;

import br.com.fiaprestaurante.restaurantepedido.domain.valueobject.StatusPedido;
import br.com.fiaprestaurante.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitarios da entidade de dominio {@link Pedido} - cobrem
 * invariantes do construtor, calculo do valor total, transicoes de
 * status, idempotencia e o construtor de hidratacao.
 *
 * @author Danilo Fernando
 */
class PedidoTest {

    private static final UUID CLIENTE_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID RESTAURANTE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID PRODUTO_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID PAGAMENTO_ID = UUID.fromString("33333333-3333-4333-8333-333333333333");

    private ItemPedido item(int qtd, String preco) {
        return new ItemPedido(PRODUTO_ID, "Produto", qtd, new BigDecimal(preco));
    }

    @Test
    void deveCriarPedidoNoStatusCriadoCalculandoValorTotal() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID,
                List.of(item(2, "25.90"), item(1, "7.50")));

        assertThat(pedido.getId()).isNotNull();
        assertThat(pedido.getClienteId()).isEqualTo(CLIENTE_ID);
        assertThat(pedido.getRestauranteId()).isEqualTo(RESTAURANTE_ID);
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CRIADO);
        assertThat(pedido.getValorTotal()).isEqualByComparingTo("59.30");
        assertThat(pedido.getItens()).hasSize(2);
        assertThat(pedido.getPagamentoId()).isNull();
        assertThat(pedido.getMotivoPendencia()).isNull();
        assertThat(pedido.getCreatedAt()).isNotNull();
        assertThat(pedido.getUpdatedAt()).isNotNull();
    }

    @Test
    void deveRejeitarClienteIdNulo() {
        assertThatThrownBy(() -> new Pedido(null, RESTAURANTE_ID, List.of(item(1, "10.00"))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("clienteId");
    }

    @Test
    void deveRejeitarRestauranteIdNulo() {
        assertThatThrownBy(() -> new Pedido(CLIENTE_ID, null, List.of(item(1, "10.00"))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("restauranteId");
    }

    @Test
    void deveRejeitarItensNulos() {
        assertThatThrownBy(() -> new Pedido(CLIENTE_ID, RESTAURANTE_ID, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("item");
    }

    @Test
    void deveRejeitarItensVazios() {
        assertThatThrownBy(() -> new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("item");
    }

    @Test
    void devolverListaDeItensImutavel() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(item(1, "10.00")));

        assertThatThrownBy(() -> pedido.getItens().add(item(1, "5.00")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void deveConfirmarPedidoCriado() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(item(1, "10.00")));

        pedido.confirmar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CONFIRMADO);
    }

    @Test
    void deveRejeitarConfirmacaoDePedidoJaConfirmado() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(item(1, "10.00")));
        pedido.confirmar();

        assertThatThrownBy(pedido::confirmar)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CONFIRMADO");
    }

    @Test
    void deveRejeitarConfirmacaoDePedidoCancelado() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(item(1, "10.00")));
        pedido.cancelar();

        assertThatThrownBy(pedido::confirmar)
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void deveMarcarComoPendentePagamento() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(item(1, "10.00")));
        pedido.confirmar();

        pedido.marcarComoPendentePagamento("gateway fora do ar");

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PENDENTE_PAGAMENTO);
        assertThat(pedido.getMotivoPendencia()).isEqualTo("gateway fora do ar");
    }

    @Test
    void naoDeveSobrescreverStatusPagoAoMarcarComoPendente() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(item(1, "10.00")));
        pedido.confirmar();
        pedido.marcarComoPago(PAGAMENTO_ID);

        pedido.marcarComoPendentePagamento("ruido tardio");

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(pedido.getMotivoPendencia()).isNull();
    }

    @Test
    void naoDeveSobrescreverStatusCanceladoAoMarcarComoPendente() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(item(1, "10.00")));
        pedido.cancelar();

        pedido.marcarComoPendentePagamento("evento perdido");

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
    }

    @Test
    void deveMarcarComoPagoAposConfirmado() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(item(1, "10.00")));
        pedido.confirmar();

        pedido.marcarComoPago(PAGAMENTO_ID);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(pedido.getPagamentoId()).isEqualTo(PAGAMENTO_ID);
        assertThat(pedido.getMotivoPendencia()).isNull();
    }

    @Test
    void deveMarcarComoPagoAposPendente() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(item(1, "10.00")));
        pedido.confirmar();
        pedido.marcarComoPendentePagamento("gateway fora");

        pedido.marcarComoPago(PAGAMENTO_ID);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(pedido.getMotivoPendencia()).isNull();
    }

    @Test
    void marcarComoPagoDeveSerIdempotente() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(item(1, "10.00")));
        pedido.confirmar();
        pedido.marcarComoPago(PAGAMENTO_ID);
        Instant primeiraAtualizacao = pedido.getUpdatedAt();

        pedido.marcarComoPago(PAGAMENTO_ID);

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(pedido.getUpdatedAt()).isEqualTo(primeiraAtualizacao);
    }

    @Test
    void naoDevePermitirMarcarComoPagoQuandoCancelado() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(item(1, "10.00")));
        pedido.cancelar();

        assertThatThrownBy(() -> pedido.marcarComoPago(PAGAMENTO_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cancelado");
    }

    @Test
    void deveCancelarPedido() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(item(1, "10.00")));

        pedido.cancelar();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.CANCELADO);
    }

    @Test
    void naoDevePermitirCancelarPedidoPago() {
        Pedido pedido = new Pedido(CLIENTE_ID, RESTAURANTE_ID, List.of(item(1, "10.00")));
        pedido.confirmar();
        pedido.marcarComoPago(PAGAMENTO_ID);

        assertThatThrownBy(pedido::cancelar)
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pago");
    }

    @Test
    void construtorDeHidratacaoDevePreservarTodosOsCampos() {
        UUID id = UUID.randomUUID();
        Instant criado = Instant.parse("2026-01-01T10:00:00Z");
        Instant atualizado = Instant.parse("2026-01-02T11:00:00Z");
        List<ItemPedido> itens = List.of(item(2, "25.90"));

        Pedido pedido = new Pedido(id, CLIENTE_ID, RESTAURANTE_ID, itens,
                new BigDecimal("51.80"), StatusPedido.PAGO, PAGAMENTO_ID,
                null, criado, atualizado);

        assertThat(pedido.getId()).isEqualTo(id);
        assertThat(pedido.getClienteId()).isEqualTo(CLIENTE_ID);
        assertThat(pedido.getRestauranteId()).isEqualTo(RESTAURANTE_ID);
        assertThat(pedido.getValorTotal()).isEqualByComparingTo("51.80");
        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
        assertThat(pedido.getPagamentoId()).isEqualTo(PAGAMENTO_ID);
        assertThat(pedido.getCreatedAt()).isEqualTo(criado);
        assertThat(pedido.getUpdatedAt()).isEqualTo(atualizado);
        assertThat(pedido.getItens()).hasSize(1);
    }
}
